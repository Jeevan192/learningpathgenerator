import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './AdminPanel.css';

const AdminPanel = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('users');
    const [users, setUsers] = useState([]);
    const [topics, setTopics] = useState([]);
    const [questions, setQuestions] = useState([]);
    const [stats, setStats] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Forms
    const [newTopic, setNewTopic] = useState({ name: '', description: '' });
    const [newQuestion, setNewQuestion] = useState({
        topic: '',
        questionText: '',
        options: ['', '', '', ''],
        correctAnswer: 0,
        difficulty: 'MEDIUM'
    });
    const [selectedTopic, setSelectedTopic] = useState(null);
    const [generatingQuestions, setGeneratingQuestions] = useState(false);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        setLoading(true);
        try {
            const [usersRes, topicsRes, questionsRes, statsRes] = await Promise.allSettled([
                api.get('/admin/users'),
                api.get('/admin/topics'),
                api.get('/admin/questions'),
                api.get('/admin/stats')
            ]);

            if (usersRes.status === 'fulfilled') setUsers(usersRes.value.data || []);
            if (topicsRes.status === 'fulfilled') setTopics(topicsRes.value.data || []);
            if (questionsRes.status === 'fulfilled') setQuestions(questionsRes.value.data || []);
            if (statsRes.status === 'fulfilled') setStats(statsRes.value.data || {});
        } catch (err) {
            setError('Failed to load data');
        } finally {
            setLoading(false);
        }
    };

    const showMessage = (msg, isError = false) => {
        if (isError) {
            setError(msg);
            setTimeout(() => setError(''), 4000);
        } else {
            setSuccess(msg);
            setTimeout(() => setSuccess(''), 4000);
        }
    };

    // User Management
    const handleUpdateUserRole = async (userId, newRole) => {
        try {
            await api.put(`/admin/users/${userId}/role`, { role: newRole });
            setUsers(users.map(u => u.id === userId ? { ...u, role: newRole } : u));
            showMessage('User role updated');
        } catch (err) {
            showMessage('Failed to update role', true);
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('Delete this user? This cannot be undone.')) return;
        try {
            await api.delete(`/admin/users/${userId}`);
            setUsers(users.filter(u => u.id !== userId));
            showMessage('User deleted');
        } catch (err) {
            showMessage('Failed to delete user', true);
        }
    };

    const handleToggleUserStatus = async (userId, currentStatus) => {
        try {
            await api.put(`/admin/users/${userId}/status`, { enabled: !currentStatus });
            setUsers(users.map(u => u.id === userId ? { ...u, enabled: !currentStatus } : u));
            showMessage('User status updated');
        } catch (err) {
            showMessage('Failed to update status', true);
        }
    };

    // Topic Management
    const handleAddTopic = async (e) => {
        e.preventDefault();
        if (!newTopic.name.trim()) {
            showMessage('Topic name is required', true);
            return;
        }
        try {
            const response = await api.post('/admin/topics', newTopic);
            setTopics([...topics, response.data]);
            setNewTopic({ name: '', description: '' });
            showMessage('Topic added successfully');
        } catch (err) {
            showMessage('Failed to add topic', true);
        }
    };

    const handleDeleteTopic = async (topicId) => {
        if (!window.confirm('Delete this topic and all its questions?')) return;
        try {
            await api.delete(`/admin/topics/${topicId}`);
            setTopics(topics.filter(t => t.id !== topicId));
            // Also remove questions for this topic
            const topic = topics.find(t => t.id === topicId);
            if (topic) {
                setQuestions(questions.filter(q => q.topic !== topic.title));
            }
            showMessage('Topic deleted');
        } catch (err) {
            showMessage('Failed to delete topic', true);
        }
    };

    // Question Management
    const handleAddQuestion = async (e) => {
        e.preventDefault();
        if (!newQuestion.topic || !newQuestion.questionText) {
            showMessage('Topic and question text are required', true);
            return;
        }
        if (newQuestion.options.some(opt => !opt.trim())) {
            showMessage('All options are required', true);
            return;
        }
        try {
            const response = await api.post('/admin/questions', {
                topic: newQuestion.topic,
                questionText: newQuestion.questionText,
                options: newQuestion.options,
                correctAnswer: newQuestion.correctAnswer,
                difficulty: newQuestion.difficulty
            });
            setQuestions([...questions, response.data]);
            setNewQuestion({
                topic: '',
                questionText: '',
                options: ['', '', '', ''],
                correctAnswer: 0,
                difficulty: 'MEDIUM'
            });
            showMessage('Question added');
        } catch (err) {
            showMessage('Failed to add question', true);
        }
    };

    const handleDeleteQuestion = async (questionId) => {
        if (!window.confirm('Delete this question?')) return;
        try {
            await api.delete(`/admin/questions/${questionId}`);
            setQuestions(questions.filter(q => q.id !== questionId));
            showMessage('Question deleted');
        } catch (err) {
            showMessage('Failed to delete question', true);
        }
    };

    const handleOptionChange = (index, value) => {
        const newOptions = [...newQuestion.options];
        newOptions[index] = value;
        setNewQuestion({ ...newQuestion, options: newOptions });
    };

    // AI Question Generation
    const handleGenerateAIQuestions = async (topicId) => {
        setGeneratingQuestions(true);
        setSelectedTopic(topicId);
        try {
            const response = await api.post(`/admin/topics/${topicId}/generate-questions?count=5`);
            showMessage(`Generated ${response.data.count} questions using AI`);
            // Refresh questions
            const questionsRes = await api.get('/admin/questions');
            setQuestions(questionsRes.data || []);
        } catch (err) {
            showMessage('Failed to generate questions. Check AI configuration.', true);
        } finally {
            setGeneratingQuestions(false);
            setSelectedTopic(null);
        }
    };

    if (loading) {
        return (
            <div className="admin-loading">
                <div className="loader"></div>
                <p>Loading Admin Panel...</p>
            </div>
        );
    }

    return (
        <div className="admin-container">
            <header className="admin-header">
                <div className="header-left">
                    <h1>⚙️ Admin Panel</h1>
                    <span>Manage your learning platform</span>
                </div>
                <button className="back-btn" onClick={() => navigate('/dashboard')}>
                    ← Back to Dashboard
                </button>
            </header>

            {error && <div className="alert error">❌ {error}</div>}
            {success && <div className="alert success">✅ {success}</div>}

            {/* Stats Overview */}
            <section className="stats-overview">
                <div className="stat-box">
                    <span className="stat-num">{stats.totalUsers || users.length}</span>
                    <span className="stat-label">Users</span>
                </div>
                <div className="stat-box">
                    <span className="stat-num">{stats.totalTopics || topics.length}</span>
                    <span className="stat-label">Topics</span>
                </div>
                <div className="stat-box">
                    <span className="stat-num">{stats.totalQuestions || questions.length}</span>
                    <span className="stat-label">Questions</span>
                </div>
                <div className="stat-box">
                    <span className="stat-num">{stats.totalQuizzes || 0}</span>
                    <span className="stat-label">Quizzes Taken</span>
                </div>
                <div className="stat-box">
                    <span className="stat-num">{stats.averageQuizScore || 0}%</span>
                    <span className="stat-label">Avg Score</span>
                </div>
            </section>

            {/* Tabs */}
            <div className="admin-tabs">
                <button
                    className={activeTab === 'users' ? 'active' : ''}
                    onClick={() => setActiveTab('users')}
                >
                    👥 Users ({users.length})
                </button>
                <button
                    className={activeTab === 'topics' ? 'active' : ''}
                    onClick={() => setActiveTab('topics')}
                >
                    📚 Topics ({topics.length})
                </button>
                <button
                    className={activeTab === 'questions' ? 'active' : ''}
                    onClick={() => setActiveTab('questions')}
                >
                    ❓ Questions ({questions.length})
                </button>
            </div>

            <main className="admin-content">
                {/* Users Tab */}
                {activeTab === 'users' && (
                    <div className="tab-content">
                        <h2>👥 User Management</h2>
                        <div className="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Username</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Points</th>
                                        <th>Level</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {users.map(user => (
                                        <tr key={user.id}>
                                            <td>{user.id}</td>
                                            <td>{user.username}</td>
                                            <td>{user.email || '-'}</td>
                                            <td>
                                                <select
                                                    value={user.role}
                                                    onChange={(e) => handleUpdateUserRole(user.id, e.target.value)}
                                                    className="role-select"
                                                >
                                                    <option value="USER">User</option>
                                                    <option value="ADMIN">Admin</option>
                                                </select>
                                            </td>
                                            <td>
                                                <span className={`status-badge ${user.enabled !== false ? 'active' : 'inactive'}`}>
                                                    {user.enabled !== false ? '✓ Active' : '✗ Disabled'}
                                                </span>
                                            </td>
                                            <td>{user.totalPoints || 0}</td>
                                            <td>{user.level || 1}</td>
                                            <td className="actions">
                                                <button
                                                    className="btn-toggle"
                                                    onClick={() => handleToggleUserStatus(user.id, user.enabled !== false)}
                                                >
                                                    {user.enabled !== false ? 'Disable' : 'Enable'}
                                                </button>
                                                <button
                                                    className="btn-delete"
                                                    onClick={() => handleDeleteUser(user.id)}
                                                >
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {users.length === 0 && (
                                <p className="no-data">No users found</p>
                            )}
                        </div>
                    </div>
                )}

                {/* Topics Tab */}
                {activeTab === 'topics' && (
                    <div className="tab-content">
                        <h2>📚 Topic Management</h2>

                        <form className="add-form" onSubmit={handleAddTopic}>
                            <input
                                type="text"
                                placeholder="Topic Name *"
                                value={newTopic.name}
                                onChange={(e) => setNewTopic({ ...newTopic, name: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="Description (optional)"
                                value={newTopic.description}
                                onChange={(e) => setNewTopic({ ...newTopic, description: e.target.value })}
                            />
                            <button type="submit">+ Add Topic</button>
                        </form>

                        <div className="topics-grid">
                            {topics.map((topic) => (
                                <div key={topic.id} className="topic-card">
                                    <div className="topic-info">
                                        <span className="topic-icon">📘</span>
                                        <div>
                                            <h3>{topic.title || topic.name}</h3>
                                            <p>{topic.description || 'No description'}</p>
                                            <span className="question-count">
                                                {questions.filter(q => q.topic === topic.title).length} questions
                                            </span>
                                        </div>
                                    </div>
                                    <div className="topic-actions">
                                        <button
                                            className="btn-generate"
                                            onClick={() => handleGenerateAIQuestions(topic.id)}
                                            disabled={generatingQuestions}
                                        >
                                            {generatingQuestions && selectedTopic === topic.id
                                                ? '⏳ Generating...'
                                                : '🤖 Generate AI Questions'}
                                        </button>
                                        <button
                                            className="btn-delete-sm"
                                            onClick={() => handleDeleteTopic(topic.id)}
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </div>
                            ))}
                            {topics.length === 0 && (
                                <p className="no-data">No topics yet. Add one above!</p>
                            )}
                        </div>
                    </div>
                )}

                {/* Questions Tab */}
                {activeTab === 'questions' && (
                    <div className="tab-content">
                        <h2>❓ Question Management</h2>

                        <form className="question-form" onSubmit={handleAddQuestion}>
                            <div className="form-row">
                                <select
                                    value={newQuestion.topic}
                                    onChange={(e) => setNewQuestion({ ...newQuestion, topic: e.target.value })}
                                    required
                                >
                                    <option value="">Select Topic *</option>
                                    {topics.map((t) => (
                                        <option key={t.id} value={t.title || t.name}>
                                            {t.title || t.name}
                                        </option>
                                    ))}
                                </select>
                                <select
                                    value={newQuestion.difficulty}
                                    onChange={(e) => setNewQuestion({ ...newQuestion, difficulty: e.target.value })}
                                >
                                    <option value="EASY">Easy</option>
                                    <option value="MEDIUM">Medium</option>
                                    <option value="HARD">Hard</option>
                                </select>
                            </div>

                            <textarea
                                placeholder="Question Text *"
                                value={newQuestion.questionText}
                                onChange={(e) => setNewQuestion({ ...newQuestion, questionText: e.target.value })}
                                required
                            />

                            <div className="options-grid">
                                {newQuestion.options.map((opt, idx) => (
                                    <div key={idx} className="option-input">
                                        <label>
                                            <input
                                                type="radio"
                                                name="correctAnswer"
                                                checked={newQuestion.correctAnswer === idx}
                                                onChange={() => setNewQuestion({ ...newQuestion, correctAnswer: idx })}
                                            />
                                            Option {String.fromCharCode(65 + idx)} (Correct)
                                        </label>
                                        <input
                                            type="text"
                                            placeholder={`Option ${String.fromCharCode(65 + idx)} *`}
                                            value={opt}
                                            onChange={(e) => handleOptionChange(idx, e.target.value)}
                                            required
                                        />
                                    </div>
                                ))}
                            </div>

                            <button type="submit">+ Add Question</button>
                        </form>

                        <div className="questions-list">
                            <h3>📋 All Questions ({questions.length})</h3>
                            {questions.map((q) => (
                                <div key={q.id} className="question-item">
                                    <div className="question-header">
                                        <span className="topic-tag">{q.topic}</span>
                                        <span className={`difficulty-tag ${q.difficulty?.toLowerCase()}`}>
                                            {q.difficulty}
                                        </span>
                                        <button
                                            className="btn-delete-sm"
                                            onClick={() => handleDeleteQuestion(q.id)}
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                    <p className="question-text">{q.questionText}</p>
                                    <div className="question-options">
                                        {q.options?.map((opt, idx) => (
                                            <span
                                                key={idx}
                                                className={`option-badge ${opt === q.correctAnswer ? 'correct' : ''}`}
                                            >
                                                {String.fromCharCode(65 + idx)}: {opt}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            ))}
                            {questions.length === 0 && (
                                <p className="no-data">No questions yet. Add one above or generate with AI!</p>
                            )}
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
};

export default AdminPanel;

