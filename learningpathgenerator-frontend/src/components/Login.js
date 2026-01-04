import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const Login = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    // Forgot password states
    const [showForgotPassword, setShowForgotPassword] = useState(false);
    const [forgotStep, setForgotStep] = useState(1); // 1: email/username, 2: security question, 3: new password
    const [forgotData, setForgotData] = useState({
        email: '',
        username: '',
        securityAnswer: '',
        newPassword: '',
        confirmPassword: '',
        resetToken: ''
    });
    const [securityQuestion, setSecurityQuestion] = useState('');

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleForgotChange = (e) => {
        setForgotData({ ...forgotData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post('/auth/login', formData);
            const { token, username, role, roles, id } = response.data;

            localStorage.setItem('token', token);

            let userRole = role || (roles && roles[0]) || 'USER';
            if (userRole.startsWith('ROLE_')) {
                userRole = userRole.replace('ROLE_', '');
            }

            const userObj = { id, username, role: userRole };
            localStorage.setItem('user', JSON.stringify(userObj));
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || err.response?.data || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    const handleForgotPassword = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post('/auth/forgot-password', {
                email: forgotData.email,
                username: forgotData.username
            });

            const data = response.data;
            setForgotData({ ...forgotData, resetToken: data.resetToken });

            if (data.securityQuestion) {
                setSecurityQuestion(data.securityQuestion);
                setForgotStep(2);
            } else {
                setForgotStep(3);
            }
            setSuccess(data.message);
        } catch (err) {
            setError(err.response?.data || 'Failed to process forgot password request');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifySecurityAnswer = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await api.post('/auth/verify-security-answer', {
                resetToken: forgotData.resetToken,
                securityAnswer: forgotData.securityAnswer
            });

            setForgotStep(3);
            setSuccess('Security answer verified. Please enter your new password.');
        } catch (err) {
            setError(err.response?.data || 'Incorrect security answer');
        } finally {
            setLoading(false);
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        if (forgotData.newPassword !== forgotData.confirmPassword) {
            setError('Passwords do not match');
            setLoading(false);
            return;
        }

        if (forgotData.newPassword.length < 6) {
            setError('Password must be at least 6 characters');
            setLoading(false);
            return;
        }

        try {
            await api.post('/auth/reset-password', {
                resetToken: forgotData.resetToken,
                newPassword: forgotData.newPassword
            });

            setSuccess('Password reset successfully! Please login with your new password.');
            setShowForgotPassword(false);
            setForgotStep(1);
            setForgotData({
                email: '',
                username: '',
                securityAnswer: '',
                newPassword: '',
                confirmPassword: '',
                resetToken: ''
            });
        } catch (err) {
            setError(err.response?.data || 'Failed to reset password');
        } finally {
            setLoading(false);
        }
    };

    const closeForgotPassword = () => {
        setShowForgotPassword(false);
        setForgotStep(1);
        setError('');
        setSuccess('');
        setForgotData({
            email: '',
            username: '',
            securityAnswer: '',
            newPassword: '',
            confirmPassword: '',
            resetToken: ''
        });
    };

    if (showForgotPassword) {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1>🔐 Reset Password</h1>
                        <p>
                            {forgotStep === 1 && 'Enter your username or email'}
                            {forgotStep === 2 && 'Answer your security question'}
                            {forgotStep === 3 && 'Create a new password'}
                        </p>
                    </div>

                    {error && <div className="error-msg">{error}</div>}
                    {success && <div className="success-msg">{success}</div>}

                    {forgotStep === 1 && (
                        <form onSubmit={handleForgotPassword} className="auth-form">
                            <div className="form-group">
                                <label>Username</label>
                                <input
                                    type="text"
                                    name="username"
                                    value={forgotData.username}
                                    onChange={handleForgotChange}
                                    placeholder="Enter your username"
                                />
                            </div>
                            <div className="form-group">
                                <label>Or Email</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={forgotData.email}
                                    onChange={handleForgotChange}
                                    placeholder="Enter your email"
                                />
                            </div>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Processing...' : 'Continue'}
                            </button>
                        </form>
                    )}

                    {forgotStep === 2 && (
                        <form onSubmit={handleVerifySecurityAnswer} className="auth-form">
                            <div className="security-question">
                                <p><strong>Security Question:</strong></p>
                                <p>{securityQuestion}</p>
                            </div>
                            <div className="form-group">
                                <label>Your Answer</label>
                                <input
                                    type="text"
                                    name="securityAnswer"
                                    value={forgotData.securityAnswer}
                                    onChange={handleForgotChange}
                                    placeholder="Enter your answer"
                                    required
                                />
                            </div>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Verifying...' : 'Verify Answer'}
                            </button>
                        </form>
                    )}

                    {forgotStep === 3 && (
                        <form onSubmit={handleResetPassword} className="auth-form">
                            <div className="form-group">
                                <label>New Password</label>
                                <input
                                    type="password"
                                    name="newPassword"
                                    value={forgotData.newPassword}
                                    onChange={handleForgotChange}
                                    placeholder="Enter new password"
                                    required
                                    minLength={6}
                                />
                            </div>
                            <div className="form-group">
                                <label>Confirm Password</label>
                                <input
                                    type="password"
                                    name="confirmPassword"
                                    value={forgotData.confirmPassword}
                                    onChange={handleForgotChange}
                                    placeholder="Confirm new password"
                                    required
                                />
                            </div>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Resetting...' : 'Reset Password'}
                            </button>
                        </form>
                    )}

                    <p className="auth-footer">
                        <button className="link-btn" onClick={closeForgotPassword}>
                            ← Back to Login
                        </button>
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>🎯 Learning Path Generator</h1>
                    <p>Sign in to continue</p>
                </div>

                {error && <div className="error-msg">{error}</div>}
                {success && <div className="success-msg">{success}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            autoComplete="username"
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            autoComplete="current-password"
                        />
                    </div>
                    <button type="submit" disabled={loading}>
                        {loading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>

                <div className="auth-links">
                    <button className="link-btn" onClick={() => setShowForgotPassword(true)}>
                        Forgot Password?
                    </button>
                </div>

                <p className="auth-footer">
                    Don't have an account? <Link to="/register">Register</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;

