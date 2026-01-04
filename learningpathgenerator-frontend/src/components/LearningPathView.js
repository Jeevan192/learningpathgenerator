import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { learningPathAPI } from '../services/api';
import './LearningPathView.css';

const LearningPathView = () => {
    const { pathId } = useParams();
    const navigate = useNavigate();
    const [path, setPath] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchLearningPath();
    }, [pathId]);

    const fetchLearningPath = async () => {
        try {
            setLoading(true);
            const response = await learningPathAPI.getPathById(pathId);
            setPath(response.data);
        } catch (err) {
            console.error('Error fetching learning path:', err);
            setError('Failed to load learning path');
        } finally {
            setLoading(false);
        }
    };

    const handleResourceComplete = async (resourceId) => {
        try {
            await learningPathAPI.updateProgress(pathId, resourceId, 100);
            fetchLearningPath(); // Refresh data
        } catch (err) {
            console.error('Error updating progress:', err);
        }
    };

    const handleExport = async () => {
        try {
            const response = await learningPathAPI.exportPath(pathId);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `learning-path-${pathId}.csv`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            console.error('Error exporting path:', err);
            alert('Failed to export learning path');
        }
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
                <p>Loading your learning path...</p>
            </div>
        );
    }

    if (error || !path) {
        return (
            <div className="error-container">
                <h2>⚠️ {error || 'Learning path not found'}</h2>
                <button onClick={() => navigate('/dashboard')} className="btn-primary">
                    Back to Dashboard
                </button>
            </div>
        );
    }

    return (
        <div className="learning-path-view">
            <nav className="path-nav">
                <Link to="/dashboard" className="back-link">← Back to Dashboard</Link>
                <button onClick={handleExport} className="export-btn">📥 Export to CSV</button>
            </nav>

            <header className="path-header">
                <h1>{path.title}</h1>
                <p className="path-description">{path.description}</p>

                <div className="path-stats">
                    <div className="stat">
                        <span className="stat-value">{path.resources?.length || 0}</span>
                        <span className="stat-label">Resources</span>
                    </div>
                    <div className="stat">
                        <span className="stat-value">{path.estimatedDuration || 0}</span>
                        <span className="stat-label">Minutes</span>
                    </div>
                    <div className="stat">
                        <span className="stat-value">{Math.round(path.completionPercentage || 0)}%</span>
                        <span className="stat-label">Complete</span>
                    </div>
                </div>

                <div className="overall-progress">
                    <div
                        className="progress-fill"
                        style={{ width: `${path.completionPercentage || 0}%` }}
                    ></div>
                </div>
            </header>

            <section className="resources-section">
                <h2>📚 Learning Resources</h2>

                {path.resources && path.resources.length > 0 ? (
                    <div className="resources-list">
                        {path.resources.map((resource, index) => (
                            <div key={resource.id || index} className="resource-card">
                                <div className="resource-number">{index + 1}</div>

                                <div className="resource-content">
                                    <div className="resource-header">
                                        <span className={`resource-type ${resource.resourceType?.toLowerCase()}`}>
                                            {resource.resourceType === 'VIDEO' && '🎥'}
                                            {resource.resourceType === 'ARTICLE' && '📄'}
                                            {resource.resourceType === 'QUIZ' && '❓'}
                                            {resource.resourceType === 'PRACTICE' && '💻'}
                                            {resource.resourceType || 'RESOURCE'}
                                        </span>
                                        <span className={`difficulty ${resource.difficultyLevel?.toLowerCase()}`}>
                                            {resource.difficultyLevel}
                                        </span>
                                    </div>

                                    <h3>{resource.title}</h3>
                                    <p>{resource.description}</p>

                                    {resource.estimatedDuration && (
                                        <span className="duration">⏱️ {resource.estimatedDuration} min</span>
                                    )}

                                    <div className="resource-actions">
                                        {resource.url && (
                                            <a
                                                href={resource.url}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="btn-resource"
                                            >
                                                Open Resource
                                            </a>
                                        )}
                                        <button
                                            onClick={() => handleResourceComplete(resource.id)}
                                            className="btn-complete"
                                        >
                                            ✓ Mark Complete
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p className="no-resources">No resources available for this learning path.</p>
                )}
            </section>
        </div>
    );
};

export default LearningPathView;

