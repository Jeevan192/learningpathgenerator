import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Leaderboard.css';

const Leaderboard = () => {
    const navigate = useNavigate();
    const [leaderboard, setLeaderboard] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        setCurrentUser(user);
        fetchLeaderboard();
    }, []);

    const fetchLeaderboard = async () => {
        try {
            const response = await api.get('/gamification/leaderboard');
            setLeaderboard(response.data || []);
        } catch (err) {
            console.error('Error fetching leaderboard:', err);
        } finally {
            setLoading(false);
        }
    };

    const getRankIcon = (rank) => {
        if (rank === 1) return '🥇';
        if (rank === 2) return '🥈';
        if (rank === 3) return '🥉';
        return `#${rank}`;
    };

    if (loading) {
        return <div className="leaderboard-loading"><div className="loader"></div></div>;
    }

    return (
        <div className="leaderboard-container">
            <header className="leaderboard-header">
                <h1>🏆 Leaderboard</h1>
                <button onClick={() => navigate('/dashboard')}>← Dashboard</button>
            </header>

            <div className="leaderboard-content">
                {/* Top 3 Podium */}
                <div className="podium">
                    {leaderboard.slice(0, 3).map((entry, idx) => (
                        <div key={entry.userId} className={`podium-place place-${idx + 1}`}>
                            <div className="podium-avatar">{entry.username?.charAt(0).toUpperCase()}</div>
                            <span className="podium-rank">{getRankIcon(idx + 1)}</span>
                            <span className="podium-name">{entry.username}</span>
                            <span className="podium-points">{entry.totalPoints} pts</span>
                        </div>
                    ))}
                </div>

                {/* Full List */}
                <div className="leaderboard-list">
                    {leaderboard.map((entry, idx) => (
                        <div
                            key={entry.userId}
                            className={`leaderboard-row ${entry.username === currentUser?.username ? 'current-user' : ''}`}
                        >
                            <span className="rank">{getRankIcon(idx + 1)}</span>
                            <div className="user-info">
                                <span className="avatar">{entry.username?.charAt(0).toUpperCase()}</span>
                                <span className="username">{entry.username}</span>
                            </div>
                            <div className="stats">
                                <span className="level">Lvl {entry.level || 1}</span>
                                <span className="streak">🔥 {entry.currentStreak || 0}</span>
                                <span className="points">{entry.totalPoints} pts</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Leaderboard;
