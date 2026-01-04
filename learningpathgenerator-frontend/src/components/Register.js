import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const Register = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        securityQuestion: '',
        securityAnswer: '',
        adminSecretKey: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const [registerAsAdmin, setRegisterAsAdmin] = useState(false);

    const securityQuestions = [
        "What is your mother's maiden name?",
        "What was the name of your first pet?",
        "What city were you born in?",
        "What is your favorite book?",
        "What was the name of your first school?",
        "What is your favorite movie?",
        "What was your childhood nickname?"
    ];

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (formData.password.length < 6) {
            setError('Password must be at least 6 characters');
            return;
        }

        if (registerAsAdmin) {
            if (!formData.securityQuestion || !formData.securityAnswer) {
                setError('Security question and answer are required for admin registration');
                return;
            }
            if (!formData.adminSecretKey) {
                setError('Admin secret key is required');
                return;
            }
        }

        setLoading(true);
        setError('');

        try {
            const endpoint = registerAsAdmin ? '/auth/register-admin' : '/auth/register';
            const payload = {
                username: formData.username,
                email: formData.email,
                password: formData.password
            };

            if (registerAsAdmin) {
                payload.securityQuestion = formData.securityQuestion;
                payload.securityAnswer = formData.securityAnswer;
                payload.adminSecretKey = formData.adminSecretKey;
            }

            const response = await api.post(endpoint, payload);

            // Auto-login after registration
            const { token, username, role } = response.data;
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ username, role }));

            setSuccess('Registration successful! Redirecting...');
            setTimeout(() => navigate('/dashboard'), 1500);

        } catch (err) {
            setError(err.response?.data?.message || err.response?.data || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>🎯 Create Account</h1>
                    <p>Join the learning community</p>
                </div>

                {error && <div className="error-msg">{error}</div>}
                {success && <div className="success-msg">{success}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label>Username *</label>
                        <input
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            autoComplete="username"
                            placeholder="Choose a username"
                        />
                    </div>

                    <div className="form-group">
                        <label>Email *</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            autoComplete="email"
                            placeholder="your@email.com"
                        />
                    </div>

                    <div className="form-group">
                        <label>Password *</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            autoComplete="new-password"
                            placeholder="At least 6 characters"
                            minLength={6}
                        />
                    </div>

                    <div className="form-group">
                        <label>Confirm Password *</label>
                        <input
                            type="password"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                            autoComplete="new-password"
                            placeholder="Confirm your password"
                        />
                    </div>

                    <div className="admin-toggle">
                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={registerAsAdmin}
                                onChange={(e) => setRegisterAsAdmin(e.target.checked)}
                            />
                            <span>Register as Admin</span>
                        </label>
                    </div>

                    {registerAsAdmin && (
                        <div className="admin-fields">
                            <div className="admin-notice">
                                <p>⚠️ Admin registration requires a secret key and security question for account recovery.</p>
                            </div>

                            <div className="form-group">
                                <label>Admin Secret Key *</label>
                                <input
                                    type="password"
                                    name="adminSecretKey"
                                    value={formData.adminSecretKey}
                                    onChange={handleChange}
                                    placeholder="Enter admin secret key"
                                />
                                <small>Contact your administrator for the secret key</small>
                            </div>

                            <div className="form-group">
                                <label>Security Question *</label>
                                <select
                                    name="securityQuestion"
                                    value={formData.securityQuestion}
                                    onChange={handleChange}
                                >
                                    <option value="">Select a security question</option>
                                    {securityQuestions.map((q, i) => (
                                        <option key={i} value={q}>{q}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Security Answer *</label>
                                <input
                                    type="text"
                                    name="securityAnswer"
                                    value={formData.securityAnswer}
                                    onChange={handleChange}
                                    placeholder="Your answer (case-insensitive)"
                                />
                            </div>
                        </div>
                    )}

                    <button type="submit" disabled={loading}>
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>

                <p className="auth-footer">
                    Already have an account? <Link to="/login">Sign In</Link>
                </p>
            </div>
        </div>
    );
};

export default Register;

