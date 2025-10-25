import React, { useState } from 'react';
import axios from 'axios';
import './GeneratorPage.css';

const GeneratorPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        skillLevel: 'BEGINNER',
        interests: '',
        weeklyHours: 6,
        target: ''
    });
    const [learningPath, setLearningPath] = useState(null);
    const [loading, setLoading] = useState(false);

    const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const generatePath = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            const payload = {
                ...formData,
                interests: formData.interests.split(',').map(i => i.trim()).filter(i => i),
                weeklyHours: parseInt(formData.weeklyHours)
            };

            const response = await axios.post(
                `${API_URL}/api/learning-path/generate`,
                payload,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            setLearningPath(response.data);
        } catch (error) {
            console.error('Error generating path:', error);
            alert('Failed to generate learning path');
        } finally {
            setLoading(false);
        }
    };

    const exportToCSV = async () => {
        if (!learningPath) return;

        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(
                `${API_URL}/api/export/learning-path/csv`,
                learningPath,
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
        <div className="generator-container">
            <h1>🎯 Generate Your Learning Path</h1>

            {!learningPath ? (
                <form onSubmit={generatePath} className="generator-form">
                    <div className="form-group">
                        <label>Your Name</label>
                        <input
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Skill Level</label>
                        <select name="skillLevel" value={formData.skillLevel} onChange={handleChange}>
                            <option value="BEGINNER">Beginner</option>
                            <option value="INTERMEDIATE">Intermediate</option>
                            <option value="ADVANCED">Advanced</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Interests (comma-separated)</label>
                        <input
                            type="text"
                            name="interests"
                            value={formData.interests}
                            onChange={handleChange}
                            placeholder="e.g., web, algorithms, databases"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Weekly Hours Available</label>
                        <input
                            type="number"
                            name="weeklyHours"
                            value={formData.weeklyHours}
                            onChange={handleChange}
                            min="1"
                            max="40"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Career Target</label>
                        <input
                            type="text"
                            name="target"
                            value={formData.target}
                            onChange={handleChange}
                            placeholder="e.g., Full Stack Developer"
                            required
                        />
                    </div>

                    <button type="submit" disabled={loading} className="btn-primary">
                        {loading ? 'Generating...' : 'Generate Learning Path'}
                    </button>
                </form>
            ) : (
                <div className="learning-path-result">
                    <h2>{learningPath.title}</h2>
                    <div className="path-summary">
                        <div className="summary-card">
                            <p><strong>Skill Level:</strong> {learningPath.skillLevel}</p>
                            <p><strong>Weekly Hours:</strong> {learningPath.weeklyHours}</p>
                            <p><strong>Estimated Weeks:</strong> {learningPath.estimatedWeeks}</p>
                            <p><strong>Total Hours:</strong> {learningPath.totalHours}</p>
                        </div>
                    </div>

                    <h3>Learning Modules</h3>
                    <div className="modules-grid">
                        {learningPath.modules.map((module, idx) => (
                            <div key={idx} className="module-card">
                                <h4>{module.title}</h4>
                                <p>{module.description}</p>
                                <p className="hours"><strong>{module.hours} hours</strong></p>
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

                    <div className="path-actions">
                        <button onClick={() => setLearningPath(null)} className="btn-secondary">
                            Generate New Path
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

export default GeneratorPage;