import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Quiz.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Quiz({ currentUser, token, onBack, onPathGenerated }) {
  const [topics, setTopics] = useState([]);
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [answers, setAnswers] = useState([]);
  const [score, setScore] = useState(0);
  const [quizComplete, setQuizComplete] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    name: currentUser || '',
    skillLevel: 'BEGINNER',
    weeklyHours: 10,
    target: ''
  });

  useEffect(() => {
    loadTopics();
  }, [token]);

  const loadTopics = async () => {
    setLoading(true);
    setError('');
    
    try {
      console.log('🔍 Loading topics...');
      console.log('🔑 Token present:', token ? 'YES' : 'NO');
      
      const response = await axios.get(`${API_URL}/api/quiz/topics`, {
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('📦 RAW Response:', response);
      console.log('📋 Response Data:', response.data);
      console.log('📊 Data Type:', Array.isArray(response.data) ? 'Array' : typeof response.data);
      console.log('📏 Data Length:', response.data?.length);
      
      if (response.data && response.data.length > 0) {
        console.log('🔬 First Topic Structure:', JSON.stringify(response.data[0], null, 2));
        console.log('🔬 First Topic Keys:', Object.keys(response.data[0]));
      }
      
      // Handle the response data
      let topicsData = Array.isArray(response.data) ? response.data : [];
      
      console.log('✅ Topics Data Array:', topicsData);
      
      // Map topics to ensure correct structure
      const mappedTopics = topicsData.map((topic, index) => {
        console.log(`\n🔍 Processing Topic ${index + 1}:`, topic);
        
        // Extract ID (try different possible field names)
        const topicId = topic.id || topic.topicId || topic._id || `topic-${index}`;
        
        // Extract Name (try different possible field names)
        const topicName = topic.name || topic.topicName || topic.title || 'Unnamed Topic';
        
        // Extract Description (try different possible field names)
        const topicDesc = topic.description || topic.desc || topic.details || 'No description available';
        
        const mapped = {
          id: topicId,
          name: topicName,
          description: topicDesc
        };
        
        console.log(`✅ Mapped to:`, mapped);
        return mapped;
      });
      
      console.log('\n📋 All Mapped Topics:', mappedTopics);
      
      if (mappedTopics.length > 0) {
        setTopics(mappedTopics);
        console.log(`✅ Successfully loaded ${mappedTopics.length} topics`);
      } else {
        console.error('❌ No topics after mapping');
        setError('No quiz topics available. Please contact administrator.');
      }
      
    } catch (err) {
      console.error('❌ ERROR Loading Topics:', err);
      console.error('📛 Error Response:', err.response?.data);
      console.error('📛 Error Status:', err.response?.status);
      console.error('📛 Error Message:', err.message);
      
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message || 
                          'Failed to load quiz topics';
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleTopicSelect = async (topic) => {
    if (!topic || !topic.id) {
      console.error('❌ Invalid topic selected:', topic);
      setError('Invalid topic. Please try another one.');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      console.log('🔍 Loading questions for topic:', topic.id, '-', topic.name);
      
      const response = await axios.get(
        `${API_URL}/api/quiz/topics/${encodeURIComponent(topic.id)}/questions`,
        { 
          headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          } 
        }
      );
      
      console.log('📦 Questions response:', response.data);
      
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        setSelectedTopic(topic);
        setQuestions(response.data);
        setAnswers(new Array(response.data.length).fill(null));
        setCurrentQuestion(0);
        console.log('✅ Questions loaded successfully:', response.data.length);
      } else {
        console.warn('⚠️ No questions found for topic:', topic.id);
        setError(`No questions available for "${topic.name}". Please choose another topic or contact administrator.`);
      }
    } catch (err) {
      console.error('❌ Error loading questions:', err);
      
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message || 
                          'Failed to load questions';
      
      setError(`Failed to load questions for "${topic.name}": ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (answerIndex) => {
    const newAnswers = [...answers];
    newAnswers[currentQuestion] = answerIndex;
    setAnswers(newAnswers);
    console.log(`Answer selected for Q${currentQuestion + 1}:`, answerIndex);
  };

  const handleNext = () => {
    if (answers[currentQuestion] === null) {
      alert('⚠️ Please select an answer before proceeding.');
      return;
    }

    const isCorrect = answers[currentQuestion] === questions[currentQuestion].correctIndex;
    if (isCorrect) {
      setScore(score + 1);
      console.log('✅ Correct answer! Score:', score + 1);
    } else {
      console.log('❌ Wrong answer');
    }

    if (currentQuestion < questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
      console.log('➡️ Moving to next question:', currentQuestion + 2);
    } else {
      console.log('🏁 Quiz complete, submitting...');
      handleSubmitQuiz();
    }
  };

  const handlePrevious = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(currentQuestion - 1);
      console.log('⬅️ Moving to previous question:', currentQuestion);
    }
  };

  const handleSubmitQuiz = async () => {
    setQuizComplete(true);

    try {
      const finalScore = answers.reduce((acc, answer, index) => {
        return answer === questions[index].correctIndex ? acc + 1 : acc;
      }, 0);

      const percentage = Math.round((finalScore / questions.length) * 100);
      console.log('📊 Final Score:', finalScore, '/', questions.length, `(${percentage}%)`);
      
      setScore(finalScore);

      console.log('🎓 Generating learning path...');
      
      const generateRequest = {
        name: formData.name || currentUser,
        skillLevel: formData.skillLevel,
        weeklyHours: parseInt(formData.weeklyHours) || 10,
        target: formData.target || `${selectedTopic.name} Mastery`,
        interests: [selectedTopic.name]
      };

      console.log('📤 Learning path request:', generateRequest);

      const pathResponse = await axios.post(
        `${API_URL}/api/learning-path/generate`,
        generateRequest,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      console.log('✅ Learning path generated:', pathResponse.data);

      const progressData = {
        username: currentUser,
        completedModules: [],
        currentModule: 0,
        overallProgress: 0,
        totalModules: pathResponse.data.modules?.length || 0
      };

      console.log('💾 Saving progress:', progressData);

      const progressResponse = await axios.post(
        `${API_URL}/api/progress/update`,
        progressData,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      console.log('✅ Progress saved:', progressResponse.data);

      setTimeout(() => {
        onPathGenerated(pathResponse.data, progressResponse.data);
      }, 2000);

    } catch (err) {
      console.error('❌ Error in quiz submission:', err);
      
      const errorMessage = err.response?.data?.error || 
                          err.response?.data?.message || 
                          err.message || 
                          'Failed to generate learning path';
      
      setError(errorMessage);
      setQuizComplete(false);
    }
  };

  const getTopicIcon = (topicName) => {
    if (!topicName) return '📖';
    
    const name = String(topicName).toLowerCase();
    
    if (name.includes('java')) return '☕';
    if (name.includes('python')) return '🐍';
    if (name.includes('javascript') || name.includes('js')) return '💛';
    if (name.includes('web') || name.includes('html') || name.includes('css')) return '🌐';
    if (name.includes('database') || name.includes('sql')) return '🗄️';
    if (name.includes('data structure') || name.includes('algorithm')) return '📊';
    if (name.includes('react') || name.includes('angular') || name.includes('vue')) return '⚛️';
    if (name.includes('node')) return '🟢';
    if (name.includes('mobile') || name.includes('android') || name.includes('ios')) return '📱';
    if (name.includes('machine learning') || name.includes('ai')) return '🤖';
    if (name.includes('cloud') || name.includes('aws') || name.includes('azure')) return '☁️';
    
    return '📖';
  };

  if (loading) {
    return (
      <div className="quiz-container">
        <div className="loading-screen">
          <div className="loader"></div>
          <p>Loading quiz...</p>
        </div>
      </div>
    );
  }

  if (error && !selectedTopic) {
    return (
      <div className="quiz-container">
        <div className="error-screen">
          <div className="error-icon">⚠️</div>
          <h2>Oops! Something went wrong</h2>
          <p>{error}</p>
          <div className="error-actions">
            <button onClick={loadTopics} className="btn-primary">
              🔄 Try Again
            </button>
            <button onClick={onBack} className="btn-secondary">
              ← Go Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!selectedTopic) {
    return (
      <div className="quiz-container">
        <div className="quiz-header">
          <button onClick={onBack} className="back-button">
            ← Back to Dashboard
          </button>
          <div style={{flex: 1, textAlign: 'center'}}>
            <h1 className="quiz-title">Choose Your Learning Path</h1>
            <p className="quiz-subtitle">Select a topic to begin your assessment</p>
          </div>
        </div>

        <div className="setup-form">
          <div className="form-section">
            <h3>📋 Personal Information</h3>
            <div className="form-grid">
              <div className="form-group">
                <label>Your Name</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Enter your name"
                />
              </div>
              <div className="form-group">
                <label>Learning Goal</label>
                <input
                  type="text"
                  value={formData.target}
                  onChange={(e) => setFormData({ ...formData, target: e.target.value })}
                  placeholder="e.g., Become a Full Stack Developer"
                />
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3>🎯 Your Experience Level</h3>
            <div className="skill-level-options">
              {['BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map((level) => (
                <label key={level} className="skill-option">
                  <input
                    type="radio"
                    name="skillLevel"
                    value={level}
                    checked={formData.skillLevel === level}
                    onChange={(e) => setFormData({ ...formData, skillLevel: e.target.value })}
                  />
                  <div className="skill-card">
                    <div className="skill-icon">
                      {level === 'BEGINNER' ? '🌱' : level === 'INTERMEDIATE' ? '🔥' : '🚀'}
                    </div>
                    <div className="skill-name">{level}</div>
                    <div className="skill-description">
                      {level === 'BEGINNER' && 'Just starting out'}
                      {level === 'INTERMEDIATE' && 'Have some experience'}
                      {level === 'ADVANCED' && 'Expert level'}
                    </div>
                  </div>
                </label>
              ))}
            </div>
          </div>

          <div className="form-section">
            <h3>⏰ Weekly Commitment</h3>
            <div className="hours-selector">
              <label>Hours per week: <strong>{formData.weeklyHours}</strong></label>
              <input
                type="range"
                min="5"
                max="40"
                step="5"
                value={formData.weeklyHours}
                onChange={(e) => setFormData({ ...formData, weeklyHours: parseInt(e.target.value) })}
                className="hours-slider"
              />
              <div className="hours-labels">
                <span>5 hrs</span>
                <span>20 hrs</span>
                <span>40 hrs</span>
              </div>
            </div>
          </div>
        </div>

        <div className="topics-section">
          <h2 className="section-title">
            <span className="title-icon">📚</span>
            Select a Topic to Start
          </h2>
          
          {topics.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📭</div>
              <h3>No Topics Available</h3>
              <p>Please contact an administrator to add quiz topics</p>
              <button onClick={onBack} className="btn-primary">
                Go Back
              </button>
            </div>
          ) : (
            <div className="topics-grid">
              {topics.map((topic, index) => (
                <div
                  key={topic.id || index}
                  className="topic-selection-card"
                  onClick={() => handleTopicSelect(topic)}
                >
                  <div className="topic-icon">
                    {getTopicIcon(topic.name)}
                  </div>
                  <h3 className="topic-name">{topic.name}</h3>
                  <p className="topic-description">{topic.description}</p>
                  <div className="topic-action">
                    <span className="action-text">Start Quiz</span>
                    <span className="action-arrow">→</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  }

  if (quizComplete) {
    const percentage = Math.round((score / questions.length) * 100);
    
    return (
      <div className="quiz-container">
        <div className="completion-screen">
          <div className="completion-animation">
            <div className="checkmark-circle">
              <div className="checkmark">✓</div>
            </div>
          </div>
          <h1 className="completion-title">Quiz Complete! 🎉</h1>
          <div className="score-display">
            <div className="score-circle">
              <div className="score-percentage">{percentage}%</div>
              <div className="score-label">Score</div>
            </div>
            <div className="score-details">
              <p className="score-text">
                You got <strong>{score} out of {questions.length}</strong> questions correct!
              </p>
            </div>
          </div>
          {error ? (
            <div className="error-message-inline">
              <p>{error}</p>
              <button onClick={() => setQuizComplete(false)} className="btn-primary">
                Try Again
              </button>
            </div>
          ) : (
            <p className="generating-text">
              <span className="spinner-small"></span>
              Generating your personalized learning path...
            </p>
          )}
        </div>
      </div>
    );
  }

  const question = questions[currentQuestion];
  const progress = ((currentQuestion + 1) / questions.length) * 100;

  return (
    <div className="quiz-container">
      <div className="quiz-header">
        <button onClick={onBack} className="back-button">
          ← Back
        </button>
        <div className="quiz-info">
          <h2 className="quiz-topic">{selectedTopic.name}</h2>
          <div className="question-counter">
            Question {currentQuestion + 1} of {questions.length}
          </div>
        </div>
      </div>

      <div className="progress-container">
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${progress}%` }}></div>
        </div>
        <div className="progress-text">{Math.round(progress)}% Complete</div>
      </div>

      <div className="question-container">
        <h3 className="question-text">{question.text}</h3>
        
        <div className="options-container">
          {question.options && question.options.map((option, index) => (
            <button
              key={index}
              className={`option-button ${answers[currentQuestion] === index ? 'selected' : ''}`}
              onClick={() => handleAnswerSelect(index)}
            >
              <span className="option-letter">{String.fromCharCode(65 + index)}</span>
              <span className="option-text">{option}</span>
              {answers[currentQuestion] === index && (
                <span className="selected-indicator">✓</span>
              )}
            </button>
          ))}
        </div>
      </div>

      <div className="navigation-buttons">
        <button
          onClick={handlePrevious}
          disabled={currentQuestion === 0}
          className="nav-button prev"
        >
          ← Previous
        </button>
        <button
          onClick={handleNext}
          className="nav-button next"
          disabled={answers[currentQuestion] === null}
        >
          {currentQuestion === questions.length - 1 ? 'Submit Quiz ✓' : 'Next →'}
        </button>
      </div>
    </div>
  );
}

export default Quiz;