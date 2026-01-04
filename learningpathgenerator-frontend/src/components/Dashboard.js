import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [topics, setTopics] = useState([]);
    const [learningPaths, setLearningPaths] = useState([]);
    const [gamificationProfile, setGamificationProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showPathGenerator, setShowPathGenerator] = useState(false);
    const [generatingPath, setGeneratingPath] = useState(false);
    const [pathForm, setPathForm] = useState({
        topic: '',
        skillLevel: 'BEGINNER',
        targetDays: 30
    });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsedUser = JSON.parse(storedUser);
                setUser(parsedUser);
                console.log('[Dashboard] Loaded user:', parsedUser);
            } catch (e) {
                console.error('[Dashboard] Error parsing user:', e);
            }
        }
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        setLoading(true);
        try {
            // Fetch all data with proper error handling for each request
            const results = await Promise.allSettled([
                api.get('/quiz/topics'),
                api.get('/learning-paths'),
                api.get('/gamification/stats')
            ]);

            // Handle topics response
            if (results[0].status === 'fulfilled') {
                const topicsData = results[0].value.data;
                setTopics(Array.isArray(topicsData) ? topicsData : []);
                console.log('[Dashboard] Topics loaded:', topicsData);
            } else {
                console.warn('[Dashboard] Failed to load topics:', results[0].reason?.message);
                setTopics([]);
            }

            // Handle learning paths response
            if (results[1].status === 'fulfilled') {
                const pathsData = results[1].value.data;
                setLearningPaths(Array.isArray(pathsData) ? pathsData : []);
                console.log('[Dashboard] Learning paths loaded:', pathsData);
            } else {
                console.warn('[Dashboard] Failed to load paths:', results[1].reason?.message);
                setLearningPaths([]);
            }

            // Handle gamification profile response
            if (results[2].status === 'fulfilled') {
                setGamificationProfile(results[2].value.data);
                console.log('[Dashboard] Gamification profile loaded:', results[2].value.data);
            } else {
                console.warn('[Dashboard] Failed to load profile:', results[2].reason?.message);
                // Set default profile
                setGamificationProfile({
                    totalPoints: 0,
                    currentStreak: 0,
                    quizzesCompleted: 0,
                    level: 1
                });
            }

        } catch (err) {
            console.error('[Dashboard] Error fetching data:', err);
            setError('Failed to load some dashboard data');
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
    };

    const handleAdminPanel = () => {
        navigate('/admin');
    };

    const handleStartQuiz = (topic = null) => {
        if (topic) {
            navigate(`/quiz/${encodeURIComponent(topic)}`);
        } else {
            navigate('/quiz');
        }
    };

    const handleLeaderboard = () => {
        navigate('/leaderboard');
    };

    const handleViewPath = (pathId) => {
        navigate(`/learning-path/${pathId}`);
    };

    const handleGeneratePath = async (e) => {
        e.preventDefault();
        if (!pathForm.topic) {
            setError('Please select a topic');
            return;
        }

        setGeneratingPath(true);
        setError('');

        try {
            console.log('[Dashboard] Generating path with:', pathForm);
            const response = await api.post('/learning-paths/generate', {
                topic: pathForm.topic,
                skillLevel: pathForm.skillLevel,
                targetDays: parseInt(pathForm.targetDays)
            });

            console.log('[Dashboard] Path generated:', response.data);
            setLearningPaths(prev => [...prev, response.data]);
            setShowPathGenerator(false);
            setPathForm({ topic: '', skillLevel: 'BEGINNER', targetDays: 30 });
        } catch (err) {
            console.error('[Dashboard] Error generating path:', err);
            setError(err.response?.data?.message || 'Failed to generate learning path. Please try again.');
        } finally {
            setGeneratingPath(false);
        }
    };

    const handleDeletePath = async (pathId, e) => {
        e.stopPropagation();
        if (!window.confirm('Are you sure you want to delete this learning path?')) {
            return;
        }

        try {
            await api.delete(`/learning-paths/${pathId}`);
            setLearningPaths(prev => prev.filter(p => p.id !== pathId));
        } catch (err) {
            console.error('[Dashboard] Error deleting path:', err);
            setError('Failed to delete learning path');
        }
    };

    const getProgressPercentage = (path) => {
        if (!path.items || path.items.length === 0) return 0;
        const completed = path.items.filter(item => item.completed).length;
        return Math.round((completed / path.items.length) * 100);
    };

    const getSkillLevelColor = (level) => {
        switch (level) {
            case 'BEGINNER': return '#2ed573';
            case 'INTERMEDIATE': return '#ffa502';
            case 'ADVANCED': return '#ff4757';
            default: return '#00d4ff';
        }
    };

    const isAdmin = user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN';

    if (loading) {
        return (
            <div className="dashboard-loading">
                <div className="loader"></div>
                <p>Loading your dashboard...</p>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="header-left">
                    <h1>🎯 Learning Path Generator</h1>
                    <span className="welcome-text">Welcome back, {user?.username || 'Learner'}!</span>
                </div>
                <div className="header-buttons">
                    {isAdmin && (
                        <button className="admin-btn" onClick={handleAdminPanel}>
                            ⚙️ Admin Panel
                        </button>
                    )}
                    <button className="logout-btn" onClick={handleLogout}>
                        🚪 Logout
                    </button>
                </div>
            </header>

            <main className="dashboard-main">
                {error && (
                    <div className="error-message">
                        <span>{error}</span>
                        <button onClick={() => setError('')}>×</button>
                    </div>
                )}

                {/* Stats Section */}
                <section className="stats-section">
                    <div className="stat-card">
                        <span className="stat-icon">🏆</span>
                        <div className="stat-info">
                            <h3>{gamificationProfile?.totalPoints || 0}</h3>
                            <p>Total Points</p>
                        </div>
                    </div>
                    <div className="stat-card">
                        <span className="stat-icon">🔥</span>
                        <div className="stat-info">
                            <h3>{gamificationProfile?.currentStreak || 0}</h3>
                            <p>Day Streak</p>
                        </div>
                    </div>
                    <div className="stat-card">
                        <span className="stat-icon">📚</span>
                        <div className="stat-info">
                            <h3>{gamificationProfile?.quizzesCompleted || 0}</h3>
                            <p>Quizzes Done</p>
                        </div>
                    </div>
                    <div className="stat-card">
                        <span className="stat-icon">⭐</span>
                        <div className="stat-info">
                            <h3>Level {gamificationProfile?.level || 1}</h3>
                            <p>Current Level</p>
                        </div>
                    </div>
                </section>

                {/* Quick Actions */}
                <section className="quick-actions">
                    <h2>🚀 Quick Actions</h2>
                    <div className="action-buttons">
                        <button onClick={() => handleStartQuiz()}>
                            📝 Start Quiz
                        </button>
                        <button onClick={() => setShowPathGenerator(true)}>
                            🛤️ Generate Learning Path
                        </button>
                        <button onClick={handleLeaderboard}>
                            🏅 Leaderboard
                        </button>
                    </div>
                </section>

                {/* Path Generator Modal */}
                {showPathGenerator && (
                    <div className="modal-overlay" onClick={() => setShowPathGenerator(false)}>
                        <div className="modal-content" onClick={e => e.stopPropagation()}>
                            <h2>🛤️ Generate Personalized Learning Path</h2>
                            <form onSubmit={handleGeneratePath}>
                                <div className="form-group">
                                    <label>Topic</label>
                                    <select
                                        value={pathForm.topic}
                                        onChange={(e) => setPathForm({ ...pathForm, topic: e.target.value })}
                                        required
                                    >
                                        <option value="">Select a topic...</option>
                                        {topics.map((topic, index) => (
                                            <option key={index} value={topic}>{topic}</option>
                                        ))}
                                        <option value="custom">+ Enter custom topic</option>
                                    </select>
                                </div>

                                {pathForm.topic === 'custom' && (
                                    <div className="form-group">
                                        <label>Custom Topic</label>
                                        <input
                                            type="text"
                                            placeholder="Enter your topic..."
                                            onChange={(e) => setPathForm({ ...pathForm, topic: e.target.value })}
                                        />
                                    </div>
                                )}

                                <div className="form-group">
                                    <label>Skill Level</label>
                                    <select
                                        value={pathForm.skillLevel}
                                        onChange={(e) => setPathForm({ ...pathForm, skillLevel: e.target.value })}
                                    >
                                        <option value="BEGINNER">Beginner</option>
                                        <option value="INTERMEDIATE">Intermediate</option>
                                        <option value="ADVANCED">Advanced</option>
                                    </select>
                                </div>

                                <div className="form-group">
                                    <label>Target Days: {pathForm.targetDays}</label>
                                    <input
                                        type="range"
                                        min="7"
                                        max="90"
                                        value={pathForm.targetDays}
                                        onChange={(e) => setPathForm({ ...pathForm, targetDays: e.target.value })}
                                    />
                                    <div className="range-labels">
                                        <span>7 days</span>
                                        <span>90 days</span>
                                    </div>
                                </div>

                                <div className="modal-buttons">
                                    <button type="submit" disabled={generatingPath}>
                                        {generatingPath ? '⏳ Generating...' : '✨ Generate Path'}
                                    </button>
                                    <button type="button" onClick={() => setShowPathGenerator(false)}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}

                {/* Learning Paths */}
                <section className="learning-paths-section">
                    <h2>📖 My Learning Paths</h2>
                    {learningPaths.length > 0 ? (
                        <div className="paths-grid">
                            {learningPaths.map((path) => (
                                <div
                                    key={path.id}
                                    className="path-card"
                                    onClick={() => handleViewPath(path.id)}
                                >
                                    <div className="path-header">
                                        <h3>{path.topic || path.title}</h3>
                                        <span
                                            className="skill-badge"
                                            style={{ backgroundColor: getSkillLevelColor(path.skillLevel) }}
                                        >
                                            {path.skillLevel}
                                        </span>
                                    </div>
                                    <div className="path-progress">
                                        <div className="progress-bar">
                                            <div
                                                className="progress-fill"
                                                style={{ width: `${getProgressPercentage(path)}%` }}
                                            ></div>
                                        </div>
                                        <span className="progress-text">
                                            {getProgressPercentage(path)}% Complete
                                        </span>
                                    </div>
                                    <div className="path-meta">
                                        <span>📅 {path.targetDays || 30} days</span>
                                        <span>📚 {path.items?.length || 0} items</span>
                                    </div>
                                    <button
                                        className="delete-path-btn"
                                        onClick={(e) => handleDeletePath(path.id, e)}
                                    >
                                        🗑️
                                    </button>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <div className="empty-icon">🛤️</div>
                            <p>No learning paths yet. Generate your first personalized path!</p>
                            <button onClick={() => setShowPathGenerator(true)}>
                                🛤️ Create Your First Path
                            </button>
                        </div>
                    )}
                </section>

                {/* Topics Section */}
                <section className="topics-section">
                    <h2>📚 Available Topics</h2>
                    {topics.length > 0 ? (
                        <div className="topics-grid">
                            {topics.map((topic, index) => (
                                <div
                                    key={index}
                                    className="topic-card"
                                    onClick={() => handleStartQuiz(topic)}
                                >
                                    <span className="topic-icon">📘</span>
                                    <span className="topic-name">{topic}</span>
                                    <span className="topic-action">Start Quiz →</span>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <p>No topics available. Check back later or contact admin.</p>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
};

export default Dashboard;
