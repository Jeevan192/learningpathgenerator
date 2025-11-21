import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminPanel.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function AdminPanel({ token, role }) {
  const [topics, setTopics] = useState([]);
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [activeTab, setActiveTab] = useState('topics'); // 'topics' or 'questions'

  // Topic form state
  const [topicForm, setTopicForm] = useState({
    id: '',
    name: '',
    description: ''
  });
  const [editingTopic, setEditingTopic] = useState(null);

  // Question form state
  const [questionForm, setQuestionForm] = useState({
    id: '',
    text: '',
    options: ['', '', '', ''],
    correctIndex: 0
  });
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [showQuestionForm, setShowQuestionForm] = useState(false);

  useEffect(() => {
    loadTopics();
  }, []);

  useEffect(() => {
    if (selectedTopic) {
      loadQuestions(selectedTopic.id);
    }
  }, [selectedTopic]);

  const loadTopics = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_URL}/api/admin/quiz/topics`, {
        headers: { 
          Authorization: `Bearer ${token}`,
          Role: role 
        }
      });
      setTopics(response.data);
      showMessage('Topics loaded successfully', 'success');
    } catch (error) {
      showMessage('Failed to load topics', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const loadQuestions = async (topicId) => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_URL}/api/admin/quiz/topics/${topicId}/questions`, {
        headers: { 
          Authorization: `Bearer ${token}`,
          Role: role 
        }
      });
      setQuestions(response.data);
    } catch (error) {
      showMessage('Failed to load questions', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (text, type) => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 4000);
  };

  // Topic handlers
  const handleTopicSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const topicData = {
        ...topicForm,
        id: topicForm.id || topicForm.name.toLowerCase().replace(/\s+/g, '-')
      };

      if (editingTopic) {
        await axios.put(`${API_URL}/api/admin/quiz/topics/${editingTopic.id}`, topicData, {
          headers: { Authorization: `Bearer ${token}`, Role: role }
        });
        showMessage('✅ Topic updated successfully!', 'success');
      } else {
        await axios.post(`${API_URL}/api/admin/quiz/topics`, topicData, {
          headers: { Authorization: `Bearer ${token}`, Role: role }
        });
        showMessage('✅ Topic created successfully!', 'success');
      }

      setTopicForm({ id: '', name: '', description: '' });
      setEditingTopic(null);
      loadTopics();
    } catch (error) {
      showMessage('❌ Failed to save topic', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditTopic = (topic) => {
    setTopicForm(topic);
    setEditingTopic(topic);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDeleteTopic = async (topicId) => {
    if (!window.confirm('Are you sure you want to delete this topic and all its questions?')) {
      return;
    }

    setLoading(true);
    try {
      await axios.delete(`${API_URL}/api/admin/quiz/topics/${topicId}`, {
        headers: { Authorization: `Bearer ${token}`, Role: role }
      });
      showMessage('🗑️ Topic deleted successfully', 'success');
      if (selectedTopic?.id === topicId) {
        setSelectedTopic(null);
        setQuestions([]);
      }
      loadTopics();
    } catch (error) {
      showMessage('❌ Failed to delete topic', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelTopicEdit = () => {
    setTopicForm({ id: '', name: '', description: '' });
    setEditingTopic(null);
  };

  // Question handlers
  const handleQuestionSubmit = async (e) => {
    e.preventDefault();
    if (!selectedTopic) {
      showMessage('⚠️ Please select a topic first', 'error');
      return;
    }

    setLoading(true);
    try {
      const questionData = {
        ...questionForm,
        id: questionForm.id || undefined
      };

      if (editingQuestion) {
        await axios.put(`${API_URL}/api/admin/quiz/questions/${editingQuestion.id}`, questionData, {
          headers: { Authorization: `Bearer ${token}`, Role: role }
        });
        showMessage('✅ Question updated successfully!', 'success');
      } else {
        await axios.post(`${API_URL}/api/admin/quiz/topics/${selectedTopic.id}/questions`, questionData, {
          headers: { Authorization: `Bearer ${token}`, Role: role }
        });
        showMessage('✅ Question added successfully!', 'success');
      }

      setQuestionForm({ id: '', text: '', options: ['', '', '', ''], correctIndex: 0 });
      setEditingQuestion(null);
      setShowQuestionForm(false);
      loadQuestions(selectedTopic.id);
    } catch (error) {
      showMessage('❌ Failed to save question', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditQuestion = (question) => {
    setQuestionForm(question);
    setEditingQuestion(question);
    setShowQuestionForm(true);
    setActiveTab('questions');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDeleteQuestion = async (questionId) => {
    if (!window.confirm('Are you sure you want to delete this question?')) {
      return;
    }

    setLoading(true);
    try {
      await axios.delete(`${API_URL}/api/admin/quiz/questions/${questionId}`, {
        headers: { Authorization: `Bearer ${token}`, Role: role }
      });
      showMessage('🗑️ Question deleted successfully', 'success');
      loadQuestions(selectedTopic.id);
    } catch (error) {
      showMessage('❌ Failed to delete question', 'error');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelQuestionEdit = () => {
    setQuestionForm({ id: '', text: '', options: ['', '', '', ''], correctIndex: 0 });
    setEditingQuestion(null);
    setShowQuestionForm(false);
  };

  const handleOptionChange = (index, value) => {
    const newOptions = [...questionForm.options];
    newOptions[index] = value;
    setQuestionForm({ ...questionForm, options: newOptions });
  };

  return (
    <div className="admin-panel-container">
      {/* Header */}
      <div className="admin-header">
        <div className="admin-header-content">
          <h1>🛡️ Admin Control Panel</h1>
          <p>Manage quiz topics and questions</p>
        </div>
        <div className="admin-stats">
          <div className="stat-card">
            <div className="stat-icon">📚</div>
            <div className="stat-info">
              <div className="stat-value">{topics.length}</div>
              <div className="stat-label">Topics</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">❓</div>
            <div className="stat-info">
              <div className="stat-value">{questions.length}</div>
              <div className="stat-label">Questions</div>
            </div>
          </div>
        </div>
      </div>

      {/* Message Alert */}
      {message.text && (
        <div className={`admin-message ${message.type}`}>
          <span>{message.text}</span>
          <button onClick={() => setMessage({ text: '', type: '' })}>✕</button>
        </div>
      )}

      {/* Loading Overlay */}
      {loading && (
        <div className="loading-overlay">
          <div className="spinner"></div>
          <p>Loading...</p>
        </div>
      )}

      {/* Tabs */}
      <div className="admin-tabs">
        <button 
          className={`tab-button ${activeTab === 'topics' ? 'active' : ''}`}
          onClick={() => setActiveTab('topics')}
        >
          <span className="tab-icon">📚</span>
          Manage Topics
        </button>
        <button 
          className={`tab-button ${activeTab === 'questions' ? 'active' : ''}`}
          onClick={() => setActiveTab('questions')}
          disabled={!selectedTopic}
        >
          <span className="tab-icon">❓</span>
          Manage Questions {selectedTopic && `(${selectedTopic.name})`}
        </button>
      </div>

      {/* Topics Tab */}
      {activeTab === 'topics' && (
        <div className="admin-content">
          {/* Topic Form */}
          <div className="form-section">
            <div className="section-header">
              <h2>{editingTopic ? '✏️ Edit Topic' : '➕ Create New Topic'}</h2>
              {editingTopic && (
                <button className="btn-cancel" onClick={handleCancelTopicEdit}>
                  Cancel Edit
                </button>
              )}
            </div>
            <form onSubmit={handleTopicSubmit} className="admin-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Topic Name *</label>
                  <input
                    type="text"
                    value={topicForm.name}
                    onChange={(e) => setTopicForm({ ...topicForm, name: e.target.value })}
                    placeholder="e.g., Advanced Java Programming"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Topic ID (optional)</label>
                  <input
                    type="text"
                    value={topicForm.id}
                    onChange={(e) => setTopicForm({ ...topicForm, id: e.target.value })}
                    placeholder="Auto-generated if empty"
                    disabled={!!editingTopic}
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={topicForm.description}
                  onChange={(e) => setTopicForm({ ...topicForm, description: e.target.value })}
                  placeholder="Brief description of the topic..."
                  rows={3}
                />
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {editingTopic ? '💾 Update Topic' : '➕ Create Topic'}
              </button>
            </form>
          </div>

          {/* Topics List */}
          <div className="list-section">
            <div className="section-header">
              <h2>📚 All Topics ({topics.length})</h2>
            </div>
            <div className="topics-grid">
              {topics.map((topic) => (
                <div 
                  key={topic.id} 
                  className={`topic-card ${selectedTopic?.id === topic.id ? 'selected' : ''}`}
                  onClick={() => {
                    setSelectedTopic(topic);
                    setActiveTab('questions');
                  }}
                >
                  <div className="topic-card-header">
                    <h3>{topic.name}</h3>
                    <div className="topic-actions">
                      <button 
                        className="btn-icon btn-edit" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleEditTopic(topic);
                        }}
                        title="Edit topic"
                      >
                        ✏️
                      </button>
                      <button 
                        className="btn-icon btn-delete" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteTopic(topic.id);
                        }}
                        title="Delete topic"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                  <p className="topic-description">{topic.description || 'No description'}</p>
                  <div className="topic-footer">
                    <span className="topic-id">ID: {topic.id}</span>
                  </div>
                </div>
              ))}
            </div>
            {topics.length === 0 && (
              <div className="empty-state">
                <div className="empty-icon">📚</div>
                <h3>No topics yet</h3>
                <p>Create your first topic to get started</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Questions Tab */}
      {activeTab === 'questions' && selectedTopic && (
        <div className="admin-content">
          {/* Question Form */}
          {showQuestionForm && (
            <div className="form-section">
              <div className="section-header">
                <h2>{editingQuestion ? '✏️ Edit Question' : '➕ Add New Question'}</h2>
                <button className="btn-cancel" onClick={handleCancelQuestionEdit}>
                  Cancel
                </button>
              </div>
              <form onSubmit={handleQuestionSubmit} className="admin-form">
                <div className="form-group">
                  <label>Question Text *</label>
                  <textarea
                    value={questionForm.text}
                    onChange={(e) => setQuestionForm({ ...questionForm, text: e.target.value })}
                    placeholder="Enter your question here..."
                    rows={3}
                    required
                  />
                </div>
                <div className="options-section">
                  <label>Answer Options *</label>
                  {questionForm.options.map((option, index) => (
                    <div key={index} className="option-input-group">
                      <input
                        type="radio"
                        name="correctAnswer"
                        checked={questionForm.correctIndex === index}
                        onChange={() => setQuestionForm({ ...questionForm, correctIndex: index })}
                        className="radio-input"
                      />
                      <input
                        type="text"
                        value={option}
                        onChange={(e) => handleOptionChange(index, e.target.value)}
                        placeholder={`Option ${index + 1}`}
                        required
                        className="option-text-input"
                      />
                      <span className="option-label">
                        {questionForm.correctIndex === index && '✓ Correct'}
                      </span>
                    </div>
                  ))}
                  <p className="hint">Select the radio button next to the correct answer</p>
                </div>
                <button type="submit" className="btn-primary" disabled={loading}>
                  {editingQuestion ? '💾 Update Question' : '➕ Add Question'}
                </button>
              </form>
            </div>
          )}

          {/* Add Question Button */}
          {!showQuestionForm && (
            <div className="add-question-section">
              <button 
                className="btn-primary btn-large" 
                onClick={() => setShowQuestionForm(true)}
              >
                ➕ Add New Question to "{selectedTopic.name}"
              </button>
            </div>
          )}

          {/* Questions List */}
          <div className="list-section">
            <div className="section-header">
              <h2>❓ Questions for "{selectedTopic.name}" ({questions.length})</h2>
            </div>
            <div className="questions-list">
              {questions.map((question, index) => (
                <div key={question.id} className="question-card">
                  <div className="question-header">
                    <span className="question-number">Q{index + 1}</span>
                    <div className="question-actions">
                      <button 
                        className="btn-icon btn-edit" 
                        onClick={() => handleEditQuestion(question)}
                        title="Edit question"
                      >
                        ✏️
                      </button>
                      <button 
                        className="btn-icon btn-delete" 
                        onClick={() => handleDeleteQuestion(question.id)}
                        title="Delete question"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                  <p className="question-text">{question.text}</p>
                  <div className="question-options">
                    {question.options.map((option, optIndex) => (
                      <div 
                        key={optIndex} 
                        className={`option-item ${optIndex === question.correctIndex ? 'correct' : ''}`}
                      >
                        <span className="option-letter">{String.fromCharCode(65 + optIndex)}</span>
                        <span className="option-text">{option}</span>
                        {optIndex === question.correctIndex && (
                          <span className="correct-badge">✓ Correct</span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
            {questions.length === 0 && (
              <div className="empty-state">
                <div className="empty-icon">❓</div>
                <h3>No questions yet</h3>
                <p>Add your first question to this topic</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminPanel;