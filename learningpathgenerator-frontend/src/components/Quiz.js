import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../services/api';
import './Quiz.css';

const Quiz = () => {
    const navigate = useNavigate();
    const { topic } = useParams();
    const [topics, setTopics] = useState([]);
    const [selectedTopic, setSelectedTopic] = useState(topic || '');
    const [questions, setQuestions] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [selectedAnswer, setSelectedAnswer] = useState(null);
    const [confidence, setConfidence] = useState(50);
    const [answers, setAnswers] = useState({});
    const [confidences, setConfidences] = useState({});
    const [showResult, setShowResult] = useState(false);
    const [result, setResult] = useState(null);
    const [quizStarted, setQuizStarted] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [generatingPath, setGeneratingPath] = useState(false);

    useEffect(() => {
        fetchTopics();
        if (topic) {
            setSelectedTopic(topic);
        }
    }, [topic]);

    const fetchTopics = async () => {
        try {
            const response = await api.get('/quiz/topics');
            setTopics(response.data || []);
        } catch (err) {
            console.error('Error fetching topics:', err);
            setError('Failed to load topics');
        } finally {
            setLoading(false);
        }
    };

    const startQuiz = async () => {
        if (!selectedTopic) {
            setError('Please select a topic');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const response = await api.get(`/quiz/questions/${encodeURIComponent(selectedTopic)}`);
            const questionsData = response.data || [];

            if (questionsData.length === 0) {
                setError('No questions available for this topic. Questions will be generated.');
            }

            setQuestions(questionsData);
            setQuizStarted(true);
            setCurrentIndex(0);
            setAnswers({});
            setConfidences({});
            setSelectedAnswer(null);
            setConfidence(50);
        } catch (err) {
            console.error('Error starting quiz:', err);
            setError('Failed to load quiz questions');
        } finally {
            setLoading(false);
        }
    };

    const handleAnswer = (optionIndex) => {
        setSelectedAnswer(optionIndex);
    };

    const handleConfidenceChange = (e) => {
        setConfidence(parseInt(e.target.value));
    };

    const nextQuestion = async () => {
        if (selectedAnswer === null) {
            setError('Please select an answer');
            return;
        }

        const question = questions[currentIndex];
        const selectedOption = question.options[selectedAnswer];

        // Store answer and confidence
        const newAnswers = { ...answers, [question.id]: selectedOption };
        const newConfidences = { ...confidences, [question.id]: confidence };

        setAnswers(newAnswers);
        setConfidences(newConfidences);

        if (currentIndex < questions.length - 1) {
            setCurrentIndex(currentIndex + 1);
            setSelectedAnswer(null);
            setConfidence(50);
        } else {
            // Submit quiz
            await submitQuiz(newAnswers, newConfidences);
        }
    };

    const submitQuiz = async (finalAnswers, finalConfidences) => {
        setLoading(true);
        try {
            const response = await api.post('/quiz/submit', {
                topic: selectedTopic,
                answers: finalAnswers,
                confidence: finalConfidences
            });

            setResult(response.data);
            setShowResult(true);
        } catch (err) {
            console.error('Error submitting quiz:', err);
            setError('Failed to submit quiz');
            // Calculate result locally if server fails
            const correctCount = Object.keys(finalAnswers).reduce((count, qId) => {
                const q = questions.find(q => q.id === parseInt(qId));
                return count + (q && q.correctAnswer === finalAnswers[qId] ? 1 : 0);
            }, 0);

            setResult({
                scorePercentage: Math.round((correctCount / questions.length) * 100),
                correctCount,
                totalQuestions: questions.length,
                performance: correctCount >= questions.length * 0.7 ? 'GOOD' : 'NEEDS_IMPROVEMENT'
            });
            setShowResult(true);
        } finally {
            setLoading(false);
        }
    };

    const generateLearningPath = async () => {
        if (!result?.skillProfile) {
            navigate('/dashboard');
            return;
        }

        setGeneratingPath(true);
        try {
            const response = await api.post('/learning-paths/from-quiz', result);
            navigate(`/learning-path/${response.data.id}`);
        } catch (err) {
            console.error('Error generating path:', err);
            setError('Failed to generate learning path');
            setGeneratingPath(false);
        }
    };

    const getConfidenceLabel = (value) => {
        if (value < 30) return 'Not sure';
        if (value < 60) return 'Somewhat confident';
        if (value < 80) return 'Confident';
        return 'Very confident';
    };

    const getConfidenceColor = (value) => {
        if (value < 30) return '#ff4757';
        if (value < 60) return '#ffa502';
        if (value < 80) return '#2ed573';
        return '#00d4ff';
    };

    if (loading) {
        return (
            <div className="quiz-container">
                <div className="quiz-loading">
                    <div className="loader"></div>
                    <p>Loading...</p>
                </div>
            </div>
        );
    }

    if (showResult) {
        const percentage = result?.scorePercentage || 0;
        const isGood = percentage >= 70;

        return (
            <div className="quiz-container">
                <div className="result-card">
                    <h1>{isGood ? '🎉' : '📚'} Quiz Complete!</h1>

                    <div className="score-circle" style={{ borderColor: isGood ? '#2ed573' : '#ffa502' }}>
                        <span className="score-num">{percentage}%</span>
                    </div>

                    <div className="result-stats">
                        <p>Correct: <strong>{result?.correctCount || 0}</strong> / {result?.totalQuestions || questions.length}</p>
                        <p>Performance: <strong style={{ color: isGood ? '#2ed573' : '#ffa502' }}>
                            {result?.performance || 'N/A'}
                        </strong></p>
                    </div>

                    {result?.skillProfile && (
                        <div className="skill-analysis">
                            <h3>📊 Skill Analysis</h3>
                            <p><strong>Level:</strong> {result.skillProfile.currentLevel}</p>

                            {result.skillProfile.strengths?.length > 0 && (
                                <div className="skill-section">
                                    <h4>💪 Strengths</h4>
                                    <ul>
                                        {result.skillProfile.strengths.map((s, i) => (
                                            <li key={i}>{s}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                            {result.skillProfile.weaknesses?.length > 0 && (
                                <div className="skill-section">
                                    <h4>📝 Areas to Improve</h4>
                                    <ul>
                                        {result.skillProfile.weaknesses.map((w, i) => (
                                            <li key={i}>{w}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    )}

                    <div className="result-actions">
                        <button
                            onClick={generateLearningPath}
                            disabled={generatingPath}
                            className="generate-path-btn"
                        >
                            {generatingPath ? '⏳ Generating...' : '🛤️ Generate Learning Path'}
                        </button>
                        <button onClick={() => { setShowResult(false); setQuizStarted(false); }}>
                            🔄 Try Another Quiz
                        </button>
                        <button onClick={() => navigate('/dashboard')}>
                            🏠 Dashboard
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!quizStarted) {
        return (
            <div className="quiz-container">
                <div className="topic-selection">
                    <h1>📝 Start a Quiz</h1>
                    <p>Select a topic and test your knowledge. AI will analyze your performance and confidence levels to create a personalized learning path.</p>

                    {error && <div className="error-message">{error}</div>}

                    <div className="topic-select-wrapper">
                        <label>Select Topic:</label>
                        <select
                            value={selectedTopic}
                            onChange={(e) => setSelectedTopic(e.target.value)}
                        >
                            <option value="">Choose a topic...</option>
                            {topics.map((t, i) => (
                                <option key={i} value={t}>{t}</option>
                            ))}
                        </select>
                    </div>

                    <div className="quiz-info">
                        <p>📌 Questions will be adapted to your level</p>
                        <p>📌 Use the confidence slider for each answer</p>
                        <p>📌 AI analyzes both accuracy AND confidence</p>
                    </div>

                    <div className="button-group">
                        <button
                            onClick={startQuiz}
                            disabled={!selectedTopic}
                            className="start-btn"
                        >
                            🚀 Start Quiz
                        </button>
                        <button className="back-btn" onClick={() => navigate('/dashboard')}>
                            ← Back to Dashboard
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    const question = questions[currentIndex];

    if (!question) {
        return (
            <div className="quiz-container">
                <div className="quiz-card">
                    <h2>No questions available</h2>
                    <button onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
                </div>
            </div>
        );
    }

    return (
        <div className="quiz-container">
            <div className="quiz-card">
                {error && <div className="error-message">{error}</div>}

                <div className="quiz-header">
                    <span className="topic-badge">{selectedTopic}</span>
                    <span className="question-counter">
                        Question {currentIndex + 1} of {questions.length}
                    </span>
                </div>

                <div className="quiz-progress">
                    <div
                        className="progress-fill"
                        style={{ width: `${((currentIndex + 1) / questions.length) * 100}%` }}
                    ></div>
                </div>

                <h2 className="question-text">{question.questionText}</h2>

                <div className="options">
                    {question.options?.map((opt, idx) => (
                        <button
                            key={idx}
                            className={`option ${selectedAnswer === idx ? 'selected' : ''}`}
                            onClick={() => handleAnswer(idx)}
                        >
                            <span className="option-letter">{String.fromCharCode(65 + idx)}</span>
                            <span className="option-text">{opt}</span>
                        </button>
                    ))}
                </div>

                <div className="confidence-section">
                    <label>
                        How confident are you?
                        <span
                            className="confidence-label"
                            style={{ color: getConfidenceColor(confidence) }}
                        >
                            {getConfidenceLabel(confidence)} ({confidence}%)
                        </span>
                    </label>
                    <input
                        type="range"
                        min="0"
                        max="100"
                        value={confidence}
                        onChange={handleConfidenceChange}
                        className="confidence-slider"
                        style={{
                            background: `linear-gradient(to right, ${getConfidenceColor(confidence)} ${confidence}%, #333 ${confidence}%)`
                        }}
                    />
                    <div className="confidence-hints">
                        <span>Guessing</span>
                        <span>Certain</span>
                    </div>
                </div>

                <button
                    className="next-btn"
                    onClick={nextQuestion}
                    disabled={selectedAnswer === null}
                >
                    {currentIndex === questions.length - 1 ? '✅ Finish Quiz' : 'Next Question →'}
                </button>
            </div>
        </div>
    );
};

export default Quiz;

