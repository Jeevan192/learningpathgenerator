import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Quiz from './components/Quiz';
import AdminPanel from './components/AdminPanel';
import Leaderboard from './components/Leaderboard';
import LearningPathView from './components/LearningPathView';
import './App.css';

const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem('token');
    if (!token) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

const AdminRoute = ({ children }) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    try {
        const user = JSON.parse(userStr || '{}');
        const role = user.role?.toUpperCase() || '';
        if (role !== 'ADMIN' && role !== 'ROLE_ADMIN') {
            return <Navigate to="/dashboard" replace />;
        }
    } catch (e) {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

function App() {
    return (
        <Router>
            <div className="App">
                <Routes>
                    <Route path="/" element={<Navigate to="/login" replace />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
                    <Route path="/quiz" element={<ProtectedRoute><Quiz /></ProtectedRoute>} />
                    <Route path="/quiz/:topic" element={<ProtectedRoute><Quiz /></ProtectedRoute>} />
                    <Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />
                    <Route path="/learning-path/:pathId" element={<ProtectedRoute><LearningPathView /></ProtectedRoute>} />
                    <Route path="/admin" element={<AdminRoute><AdminPanel /></AdminRoute>} />
                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;
