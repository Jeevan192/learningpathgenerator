import React, { useState } from 'react';
import './Dashboard.css';

function Dashboard({ learningPath, progress, role, onStartQuiz, onProgressUpdate }) {
  const [expandedModule, setExpandedModule] = useState(null);

  const handleExportCSV = () => {
    if (!learningPath || !learningPath.modules) return;
    const csvRows = [
      ['Module #', 'Title', 'Description', 'Hours', 'Topics']
    ];
    learningPath.modules.forEach((module, idx) => {
      csvRows.push([
        idx + 1,
        `"${module.title}"`,
        `"${module.description}"`,
        module.hours,
        `"${(module.topics || []).join('; ')}"`
      ]);
    });
    const csvContent = csvRows.map(row => row.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${learningPath.title.replace(/\s+/g, '_')}_modules.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  const showConfetti = () => {
    const canvas = document.createElement('canvas');
    canvas.className = 'confetti-canvas';
    document.body.appendChild(canvas);
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    const pieces = Array.from({ length: 100 }, () => ({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height * 0.2,
      r: Math.random() * 8 + 4,
      c: `hsl(${Math.random()*360},100%,50%)`,
      s: Math.random() * 3 + 2
    }));
    let frame = 0;
    function draw() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      pieces.forEach(p => {
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, 2*Math.PI);
        ctx.fillStyle = p.c;
        ctx.fill();
        p.y += p.s;
        p.x += Math.sin(frame/10 + p.y/20) * 2;
      });
      frame++;
      if (frame < 80) requestAnimationFrame(draw);
      else document.body.removeChild(canvas);
    }
    draw();
  };

  const handleCompleteModule = async (moduleIndex) => {
    if (!progress || !learningPath) return;
    const completedModules = progress.completedModules || [];
    if (completedModules.includes(moduleIndex)) {
      alert('Module already completed!');
      return;
    }
    const newCompletedModules = [...completedModules, moduleIndex];
    const newProgressPercent = (newCompletedModules.length / progress.totalModules) * 100;
    const updatedProgress = {
      ...progress,
      completedModules: newCompletedModules,
      currentModule: moduleIndex + 1,
      overallProgress: newProgressPercent
    };
    const result = await onProgressUpdate(updatedProgress);
    if (result.success) {
      showConfetti();
    } else {
      alert('Failed to update progress');
    }
  };

  if (!learningPath) {
    return (
      <div className="dashboard-wrapper">
        <div className="hero-section">
          <div className="hero-background">
            <div className="hero-shape shape-1"></div>
            <div className="hero-shape shape-2"></div>
            <div className="hero-shape shape-3"></div>
          </div>
          
          <div className="hero-content">
            <div className="hero-icon">🎓</div>
            <h1 className="hero-title">Ready to Start Learning?</h1>
            <p className="hero-subtitle">Take a quick assessment quiz to generate your personalized learning path</p>
            
            <button onClick={onStartQuiz} className="cta-button">
              <span className="button-icon">🚀</span>
              <span className="button-text">Take Assessment Quiz</span>
              <span className="button-arrow">→</span>
            </button>

            <div className="features-grid">
              <div className="feature-card">
                <div className="feature-icon">📊</div>
                <h3>Personalized Path</h3>
                <p>Get a custom learning roadmap based on your skills</p>
              </div>
              <div className="feature-card">
                <div className="feature-icon">⏱️</div>
                <h3>Time Estimates</h3>
                <p>Know exactly how long each module will take</p>
              </div>
              <div className="feature-card">
                <div className="feature-icon">🎯</div>
                <h3>Track Progress</h3>
                <p>Monitor your learning journey step by step</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const completedCount = progress?.completedModules?.length || 0;
  const totalModules = learningPath.modules?.length || 0;
  const progressPercent = totalModules > 0 ? (completedCount / totalModules) * 100 : 0;
  const isAllDone = completedCount === totalModules;

  return (
    <div className="dashboard-wrapper">
      {/* Progress Header */}
      <div className="progress-hero">
        <div className="progress-hero-content">
          <div className="path-title-section">
            <h1 className="path-title">
              {learningPath.title}
              {isAllDone && <span className="trophy-badge">🏆</span>}
            </h1>
            <p className="path-description">{learningPath.description}</p>
          </div>

          <div className="stats-grid">
            <div className="stat-box">
              <div className="stat-icon">⏱️</div>
              <div className="stat-content">
                <div className="stat-value">{learningPath.estimatedWeeks}</div>
                <div className="stat-label">Weeks</div>
              </div>
            </div>
            <div className="stat-box">
              <div className="stat-icon">📚</div>
              <div className="stat-content">
                <div className="stat-value">{totalModules}</div>
                <div className="stat-label">Modules</div>
              </div>
            </div>
            <div className="stat-box highlight">
              <div className="stat-icon">✓</div>
              <div className="stat-content">
                <div className="stat-value">{completedCount}/{totalModules}</div>
                <div className="stat-label">Completed</div>
              </div>
            </div>
          </div>

          <div className="progress-section">
            <div className="progress-bar-modern">
              <div 
                className="progress-fill-modern"
                style={{ width: `${progressPercent}%` }}
              >
                <span className="progress-percentage">{Math.round(progressPercent)}%</span>
              </div>
            </div>
            <div className="motivation-badge">
              {isAllDone ? (
                <>🌟 Path Completed! You're Amazing!</>
              ) : completedCount > 0 ? (
                <>🔥 Keep Going! You're Making Great Progress!</>
              ) : (
                <>🚀 Let's Get Started on Your Journey!</>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Modules Section */}
      <div className="modules-section">
        <div className="section-header">
          <h2 className="section-title">
            <span className="title-icon">📖</span>
            Your Learning Modules
          </h2>
        </div>

        <div className="modules-grid">
          {learningPath.modules && learningPath.modules.map((module, index) => {
            const isCompleted = progress?.completedModules?.includes(index);
            const isExpanded = expandedModule === index;
            
            return (
              <div 
                key={index} 
                className={`module-box ${isCompleted ? 'completed' : ''} ${isExpanded ? 'expanded' : ''}`}
              >
                <div className="module-header-section">
                  <div className="module-number">
                    {isCompleted ? '✓' : index + 1}
                  </div>
                  <div className="module-title-section">
                    <h3 className="module-title">{module.title}</h3>
                    <div className="module-meta">
                      <span className="module-duration">
                        <span className="meta-icon">⏱️</span>
                        {module.hours} hours
                      </span>
                      {isCompleted && (
                        <span className="completion-badge">
                          <span className="badge-icon">🎉</span>
                          Completed
                        </span>
                      )}
                    </div>
                  </div>
                  <button 
                    className="expand-button"
                    onClick={() => setExpandedModule(isExpanded ? null : index)}
                  >
                    {isExpanded ? '▲' : '▼'}
                  </button>
                </div>

                <p className="module-description">{module.description}</p>

                {isExpanded && module.topics && module.topics.length > 0 && (
                  <div className="topics-section">
                    <div className="topics-header">
                      <span className="topics-icon">📋</span>
                      <strong>What You'll Learn:</strong>
                    </div>
                    <div className="topics-grid">
                      {module.topics.map((topic, tIndex) => (
                        <div key={tIndex} className="topic-chip">
                          <span className="chip-icon">✓</span>
                          {topic}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div className="module-actions">
                  {!isCompleted && !isAllDone ? (
                    <button
                      onClick={() => handleCompleteModule(index)}
                      className="action-button complete"
                    >
                      <span className="button-icon">✨</span>
                      <span>Mark as Complete</span>
                    </button>
                  ) : isCompleted ? (
                    <div className="completed-indicator">
                      <span className="check-icon">✓</span>
                      Module Completed!
                    </div>
                  ) : null}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="action-section">
        <button className="action-button-large export" onClick={handleExportCSV}>
          <span className="button-icon">📥</span>
          <span>Export as CSV</span>
        </button>
        <button className="action-button-large retake" onClick={onStartQuiz}>
          <span className="button-icon">🔄</span>
          <span>Take Another Quiz</span>
        </button>
      </div>

      {/* Admin Quick Actions */}
      {role === "ADMIN" && (
        <div className="admin-quick-section">
          <div className="admin-quick-header">
            <span className="admin-icon">🛡️</span>
            <h3>Admin Quick Access</h3>
          </div>
          <p className="admin-hint">Use the Admin Panel in the navigation to manage topics and questions</p>
        </div>
      )}
    </div>
  );
}

export default Dashboard;