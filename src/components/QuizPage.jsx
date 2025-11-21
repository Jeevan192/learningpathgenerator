import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './QuizPage.css';

const QuizPage = () => {
    const [topics, setTopics] = useState([]);
    const [selectedTopic, setSelectedTopic] = useState(null);
    const [quiz, setQuiz] = useState(null);
    const [answers, setAnswers] = useState({});
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [weeklyHours, setWeeklyHours] = useState(6);
    const [name, setName] = useState('');
    const [target, setTarget] = useState('');

    const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

    useEffect(() => {
        fetchTopics();
    }, []);

    const fetchTopics = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/api/quiz/topics`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setTopics(Array.from(response.data));
        } catch (error) {
            console.error('Error fetching topics:', error);
        }
    };

    const loadQuiz = async (topicId) => {
        try {
            setLoading(true);
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/api/quiz/${topicId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setQuiz(response.data);
            setSelectedTopic(topicId);
            setAnswers({});
            setResult(null);
        } catch (error) {
            console.error('Error loading quiz:', error);
            alert('Failed to load quiz');
        } finally {
            setLoading(false);
        }
    };

    const handleAnswerChange = (questionId, answerIndex) => {
        setAnswers({ ...answers, [questionId]: answerIndex });
    };

    const submitQuiz = async () => {
        if (Object.keys(answers).length < quiz.questions.length) {
            alert('Please answer all questions');
            return;
        }

        try {
            setLoading(true);
            const token = localStorage.getItem('token');
            const payload = {
                answers,
                weeklyHours: parseInt(weeklyHours),
                name,
                target
            };

            const response = await axios.post(
                `${API_URL}/api/quiz/${selectedTopic}/submit`,
                payload,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            setResult(response.data);
        } catch (error) {
            console.error('Error submitting quiz:', error);
            alert('Failed to submit quiz');
        } finally {
            setLoading(false);
        }
    };

    const exportToCSV = async () => {
        if (!result || !result.learningPath) return;

        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(
                `${API_URL}/api/export/learning-path/csv`,
                result.learningPath,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    responseType: 'blob'
                }
            );

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `learning-path-${Date.now()}.csv`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error('Error exporting CSV:', error);
            alert('Failed to export CSV');
        }
    };

    return (
        <div className="quiz-container">
            <h1>📝 Skill Assessment Quiz</h1>

            {!quiz && !result && (
                <div className="topic-selection">
                    <h2>Select a Quiz Topic</h2>
                    <div className="topics-grid">
                        {topics.map(topicId => (
                            <button
                                key={topicId}
                                className="topic-card"
                                onClick={() => loadQuiz(topicId)}
                                disabled={loading}
                            >
                                {topicId.replace('t-', '').replace('-', ' ').toUpperCase()}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            {quiz && !result && (
                <div className="quiz-form">
                    <h2>{quiz.topicName}</h2>

                    <div className="user-info">
                        <input
                            type="text"
                            placeholder="Your Name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Career Target (e.g., Backend Developer)"
                            value={target}
                            onChange={(e) => setTarget(e.target.value)}
                        />
                        <input
                            type="number"
                            placeholder="Weekly Hours"
                            value={weeklyHours}
                            onChange={(e) => setWeeklyHours(e.target.value)}
                            min="1"
                            max="40"
                        />
                    </div>

                    {quiz.questions.map((question, idx) => (
                        <div key={question.id} className="question-block">
                            <p className="question-text">
                                <strong>Q{idx + 1}:</strong> {question.text}
                            </p>
                            <div className="options">
                                {question.options.map((option, optIdx) => (
                                    <label key={optIdx} className="option-label">
                                        <input
                                            type="radio"
                                            name={question.id}
                                            checked={answers[question.id] === optIdx}
                                            onChange={() => handleAnswerChange(question.id, optIdx)}
                                        />
                                        {option}
                                    </label>
                                ))}
                            </div>
                        </div>
                    ))}

                    <div className="quiz-actions">
                        <button onClick={() => setQuiz(null)} className="btn-secondary">
                            Back to Topics
                        </button>
                        <button onClick={submitQuiz} disabled={loading} className="btn-primary">
                            {loading ? 'Submitting...' : 'Submit Quiz'}
                        </button>
                    </div>
                </div>
            )}

            {result && (
                <div className="quiz-result">
                    <h2>🎉 Quiz Results</h2>
                    <div className="score-card">
                        <p>Score: {(result.score * 100).toFixed(0)}%</p>
                        <p>Correct: {result.correct} / {result.total}</p>
                        <p>Inferred Skill Level: <strong>{result.inferredSkill}</strong></p>
                    </div>

                    <div className="learning-path">
                        <h3>{result.learningPath.title}</h3>
                        <div className="path-info">
                            <p>Weekly Hours: {result.learningPath.weeklyHours}</p>
                            <p>Estimated Weeks: {result.learningPath.estimatedWeeks}</p>
                            <p>Total Hours: {result.learningPath.totalHours}</p>
                        </div>

                        <h4>Learning Modules:</h4>
                        <div className="modules-list">
                            {result.learningPath.modules.map((module, idx) => (
                                <div key={idx} className="module-card">
                                    <h5>{module.title}</h5>
                                    <p>{module.description}</p>
                                    <p><strong>Hours:</strong> {module.hours}</p>
                                    {module.resources && module.resources.length > 0 && (
                                        <div className="resources">
                                            <strong>Resources:</strong>
                                            <ul>
                                                {module.resources.map((resource, rIdx) => (
                                                    <li key={rIdx}>{resource}</li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="result-actions">
                        <button onClick={() => { setQuiz(null); setResult(null); }} className="btn-secondary">
                            Take Another Quiz
                        </button>
                        <button onClick={exportToCSV} className="btn-primary">
                            📥 Export to CSV
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizPage;