import React, { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import {
  Container, Paper, Typography, TextField, Button, Box, Alert,
  AppBar, Toolbar, Tabs, Tab, Card, CardContent, Grid,
  Radio, RadioGroup, FormControlLabel, FormControl, Chip, Divider, LinearProgress,
} from '@mui/material';
import { School, Quiz as QuizIcon, Dashboard as DashboardIcon, Timeline, CheckCircle, GetApp, PlayArrow } from '@mui/icons-material';
import axios from 'axios';

const theme = createTheme({
  palette: {
    primary: { main: '#2196f3' },
    secondary: { main: '#f50057' },
    success: { main: '#4caf50' },
  },
});

function App() {
  const [currentTab, setCurrentTab] = useState(0);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [learningPath, setLearningPath] = useState(null);
  const [progress, setProgress] = useState({ completedModules: [], currentModule: 0, overallProgress: 0 });
  const [topics, setTopics] = useState([]);
  const [quiz, setQuiz] = useState(null);
  const [answers, setAnswers] = useState({});
  const [quizResult, setQuizResult] = useState(null);
  const [quizForm, setQuizForm] = useState({ name: '', target: '', weeklyHours: 10 });

  const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

  useEffect(() => {
    if (token && currentTab === 1 && topics.length === 0) {
      fetchTopics();
    }
  }, [currentTab, token]);

  const fetchTopics = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/quiz/topics`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setTopics(Array.from(response.data));
    } catch (err) {
      setError('Failed to fetch quiz topics');
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post(`${API_URL}/api/auth/login`, { username, password });
      setToken(response.data.token);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('username', username);
      setQuizForm({ ...quizForm, name: username });
    } catch (err) {
      setError('Login failed. Check credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await axios.post(`${API_URL}/api/auth/register`, { username, password });
      setSuccess('Registration successful! Please login.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Registration failed. Username may exist.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setToken(null);
    localStorage.clear();
    setLearningPath(null);
    setProgress({ completedModules: [], currentModule: 0, overallProgress: 0 });
    setCurrentTab(0);
  };

  const loadQuiz = async (topicId) => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_URL}/api/quiz/${topicId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setQuiz(response.data);
      setAnswers({});
      setQuizResult(null);
    } catch (err) {
      setError('Failed to load quiz');
    } finally {
      setLoading(false);
    }
  };

  const submitQuiz = async () => {
    if (Object.keys(answers).length < quiz.questions.length) {
      setError('Please answer all questions');
      return;
    }
    setLoading(true);
    try {
      const response = await axios.post(
        `${API_URL}/api/quiz/${quiz.topicId}/submit`,
        { answers, weeklyHours: parseInt(quizForm.weeklyHours), name: quizForm.name, target: quizForm.target },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setQuizResult(response.data);
      setLearningPath(response.data.learningPath);
      setSuccess('Quiz completed! Learning path generated.');
      setTimeout(() => { setCurrentTab(0); setSuccess(''); }, 2000);
    } catch (err) {
      setError('Failed to submit quiz');
    } finally {
      setLoading(false);
    }
  };

  const markModuleComplete = (idx) => {
    if (progress.completedModules.includes(idx)) return;
    const updated = [...progress.completedModules, idx];
    const totalModules = learningPath.modules?.length || 1;
    setProgress({
      completedModules: updated,
      currentModule: idx + 1,
      overallProgress: (updated.length / totalModules) * 100
    });
    setSuccess(`Module ${idx + 1} completed!`);
    setTimeout(() => setSuccess(''), 2000);
  };

  const generatePath = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        `${API_URL}/api/learning-path/generate`,
        { name: localStorage.getItem('username'), skillLevel: 'INTERMEDIATE', interests: ['java', 'spring'], target: 'Backend Developer', weeklyHours: 15 },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setLearningPath(response.data);
      setSuccess('Learning path generated!');
      setTimeout(() => setSuccess(''), 2000);
    } catch (err) {
      setError('Failed to generate path');
    } finally {
      setLoading(false);
    }
  };

  const exportToCSV = async () => {
    if (!learningPath) return;
    try {
      const response = await axios.post(`${API_URL}/api/export/learning-path/csv`, learningPath, {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `learning-path-${Date.now()}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      setSuccess('Exported successfully!');
      setTimeout(() => setSuccess(''), 2000);
    } catch (err) {
      setError('Export failed');
    }
  };

  // LOGIN SCREEN
  if (!token) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Container maxWidth="sm" sx={{ mt: 8 }}>
          <Paper elevation={3} sx={{ p: 4 }}>
            <Box sx={{ textAlign: 'center' }}>
              <School sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
              <Typography variant="h4" gutterBottom>Learning Path Generator</Typography>
              <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                AI-Powered Personalized Learning
              </Typography>
              {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
              {success && <Alert severity="success" sx={{ mt: 2 }}>{success}</Alert>}
              
              <Tabs value={0} sx={{ mt: 3, mb: 2 }}>
                <Tab label="Login" onClick={() => setError('')} />
                <Tab label="Register" onClick={() => setError('')} />
              </Tabs>

              <Box component="form" onSubmit={handleLogin} sx={{ mt: 3 }}>
                <TextField fullWidth label="Username" value={username} onChange={(e) => setUsername(e.target.value)} margin="normal" required />
                <TextField fullWidth label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} margin="normal" required />
                <Button type="submit" fullWidth variant="contained" size="large" disabled={loading} sx={{ mt: 3 }}>
                  {loading ? 'Please wait...' : 'LOGIN'}
                </Button>
                <Button fullWidth variant="outlined" onClick={handleRegister} sx={{ mt: 2 }}>
                  REGISTER NEW ACCOUNT
                </Button>
              </Box>
            </Box>
          </Paper>
        </Container>
      </ThemeProvider>
    );
  }

  // MAIN APP WITH TABS
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AppBar position="static">
        <Toolbar>
          <School sx={{ mr: 2 }} />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Learning Path Generator</Typography>
          <Typography sx={{ mx: 2 }}>Welcome, {localStorage.getItem('username')}!</Typography>
          <Button color="inherit" onClick={handleLogout}>LOGOUT</Button>
        </Toolbar>
      </AppBar>

      {/* TABS - THIS IS THE KEY NAVIGATION */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'white' }}>
        <Tabs value={currentTab} onChange={(e, val) => setCurrentTab(val)} centered>
          <Tab icon={<DashboardIcon />} label="Dashboard" />
          <Tab icon={<QuizIcon />} label="Take Quiz" />
          <Tab icon={<Timeline />} label="Learning Path" />
        </Tabs>
      </Box>

      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

        {/* TAB 0: DASHBOARD */}
        {currentTab === 0 && (
          <Paper sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>📊 Dashboard</Typography>
            {!learningPath ? (
              <Box>
                <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>Get Started with Your Learning Journey</Typography>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Card sx={{ height: '100%' }}>
                      <CardContent sx={{ textAlign: 'center', p: 4 }}>
                        <QuizIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
                        <Typography variant="h5" gutterBottom>Take Assessment Quiz</Typography>
                        <Typography variant="body2" color="textSecondary" paragraph>
                          Test your knowledge and get a personalized learning path based on your skill level
                        </Typography>
                        <Button variant="contained" size="large" startIcon={<PlayArrow />} onClick={() => setCurrentTab(1)} sx={{ mt: 2 }}>
                          Start Quiz
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Card sx={{ height: '100%' }}>
                      <CardContent sx={{ textAlign: 'center', p: 4 }}>
                        <Timeline sx={{ fontSize: 60, color: 'success.main', mb: 2 }} />
                        <Typography variant="h5" gutterBottom>Quick Generate Path</Typography>
                        <Typography variant="body2" color="textSecondary" paragraph>
                          Generate a default intermediate-level learning path instantly
                        </Typography>
                        <Button variant="contained" size="large" color="success" onClick={generatePath} disabled={loading} sx={{ mt: 2 }}>
                          {loading ? 'Generating...' : 'Generate Now'}
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            ) : (
              <Box>
                {/* Progress Stats */}
                <Grid container spacing={2} sx={{ mb: 4 }}>
                  <Grid item xs={12} md={3}>
                    <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
                      <CardContent sx={{ textAlign: 'center' }}>
                        <Typography variant="h3" fontWeight="bold">{progress.overallProgress.toFixed(0)}%</Typography>
                        <Typography variant="body2">Overall Progress</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <Card>
                      <CardContent sx={{ textAlign: 'center' }}>
                        <Typography variant="h3" fontWeight="bold" color="success.main">{progress.completedModules.length}</Typography>
                        <Typography variant="body2" color="textSecondary">Modules Completed</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <Card>
                      <CardContent sx={{ textAlign: 'center' }}>
                        <Typography variant="h3" fontWeight="bold" color="primary.main">{learningPath.modules?.length || 0}</Typography>
                        <Typography variant="body2" color="textSecondary">Total Modules</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <Card>
                      <CardContent sx={{ textAlign: 'center' }}>
                        <Typography variant="h3" fontWeight="bold" color="secondary.main">{learningPath.weeklyHours}h</Typography>
                        <Typography variant="body2" color="textSecondary">Hours Per Week</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>

                {/* Progress Bar */}
                <Box sx={{ mb: 4 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" fontWeight="bold">Learning Progress</Typography>
                    <Typography variant="body2" color="textSecondary">
                      {progress.completedModules.length} of {learningPath.modules?.length || 0} modules completed
                    </Typography>
                  </Box>
                  <LinearProgress variant="determinate" value={progress.overallProgress} sx={{ height: 10, borderRadius: 5 }} />
                </Box>

                {/* Module List */}
                <Typography variant="h5" gutterBottom sx={{ mt: 4 }}>Your Learning Modules</Typography>
                <Divider sx={{ mb: 3 }} />

                {learningPath.modules?.map((module, idx) => {
                  const isCompleted = progress.completedModules.includes(idx);
                  const isCurrent = progress.currentModule === idx && !isCompleted;
                  
                  return (
                    <Card key={idx} sx={{ 
                      mb: 2, 
                      bgcolor: isCompleted ? 'success.light' : 'white',
                      border: isCurrent ? 2 : 1,
                      borderColor: isCurrent ? 'primary.main' : 'divider'
                    }}>
                      <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <Box sx={{ flex: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                              {isCompleted && <CheckCircle sx={{ fontSize: 20, mr: 1, color: 'success.dark' }} />}
                              {isCurrent && <Chip label="Current" color="primary" size="small" sx={{ mr: 1 }} />}
                              <Typography variant="h6" color={isCompleted ? 'success.dark' : 'primary.main'}>
                                {idx + 1}. {module.title}
                              </Typography>
                            </Box>
                            <Typography variant="body2" color="textSecondary" paragraph>{module.description}</Typography>
                            <Typography variant="caption" color="textSecondary">⏱️ {module.hours} hours</Typography>
                            {module.resources && module.resources.length > 0 && (
                              <Box sx={{ mt: 2 }}>
                                <Typography variant="subtitle2" gutterBottom>Resources:</Typography>
                                {module.resources.map((res, rIdx) => (
                                  <Chip key={rIdx} label={res} size="small" sx={{ mr: 1, mb: 1 }} />
                                ))}
                              </Box>
                            )}
                          </Box>
                          {!isCompleted && (
                            <Button variant="contained" color="success" size="small" startIcon={<CheckCircle />} onClick={() => markModuleComplete(idx)}>
                              Mark Complete
                            </Button>
                          )}
                        </Box>
                      </CardContent>
                    </Card>
                  );
                })}

                <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                  <Button variant="outlined" onClick={() => setCurrentTab(2)}>View Full Learning Path</Button>
                  <Button variant="contained" color="success" startIcon={<GetApp />} onClick={exportToCSV}>Export to CSV</Button>
                </Box>
              </Box>
            )}
          </Paper>
        )}

        {/* TAB 1: QUIZ */}
        {currentTab === 1 && (
          <Paper sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>📝 Skill Assessment Quiz</Typography>
            <Typography variant="body1" color="textSecondary" paragraph>
              Test your knowledge and get a personalized learning path tailored to your skill level
            </Typography>

            {!quiz && !quizResult && (
              <Box sx={{ mt: 4 }}>
                <Typography variant="h5" gutterBottom>Select a Quiz Topic</Typography>
                {topics.length === 0 ? (
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <Typography color="textSecondary">Loading quiz topics...</Typography>
                  </Box>
                ) : (
                  <Grid container spacing={2} sx={{ mt: 2 }}>
                    {topics.map(topicId => (
                      <Grid item xs={12} sm={6} md={4} key={topicId}>
                        <Card 
                          sx={{ 
                            cursor: 'pointer',
                            transition: 'all 0.3s',
                            '&:hover': { transform: 'translateY(-5px)', boxShadow: 6 }
                          }}
                          onClick={() => loadQuiz(topicId)}
                        >
                          <CardContent sx={{ textAlign: 'center', py: 4 }}>
                            <QuizIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                            <Typography variant="h6">
                              {topicId.replace('t-', '').replace(/-/g, ' ').toUpperCase()}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>
                    ))}
                  </Grid>
                )}
              </Box>
            )}

            {quiz && !quizResult && (
              <Box sx={{ mt: 4 }}>
                <Typography variant="h5" gutterBottom>{quiz.topicName}</Typography>
                
                <Box sx={{ mb: 4, p: 3, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="h6" gutterBottom>Your Information</Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={4}>
                      <TextField fullWidth label="Name" value={quizForm.name} onChange={(e) => setQuizForm({...quizForm, name: e.target.value})} />
                    </Grid>
                    <Grid item xs={12} md={4}>
                      <TextField fullWidth label="Career Target" placeholder="e.g., Backend Developer" value={quizForm.target} onChange={(e) => setQuizForm({...quizForm, target: e.target.value})} />
                    </Grid>
                    <Grid item xs={12} md={4}>
                      <TextField fullWidth type="number" label="Weekly Hours" value={quizForm.weeklyHours} onChange={(e) => setQuizForm({...quizForm, weeklyHours: e.target.value})} inputProps={{ min: 1, max: 40 }} />
                    </Grid>
                  </Grid>
                </Box>

                {quiz.questions?.map((q, idx) => (
                  <Card key={q.id} sx={{ mb: 3 }}>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>Question {idx + 1}: {q.text}</Typography>
                      <FormControl component="fieldset" fullWidth>
                        <RadioGroup 
                          value={answers[q.id] !== undefined ? answers[q.id] : ''} 
                          onChange={(e) => setAnswers({...answers, [q.id]: parseInt(e.target.value)})}
                        >
                          {q.options?.map((opt, optIdx) => (
                            <FormControlLabel 
                              key={optIdx} 
                              value={optIdx} 
                              control={<Radio />} 
                              label={opt}
                              sx={{ 
                                p: 1, 
                                m: 0.5, 
                                border: 1, 
                                borderColor: 'divider', 
                                borderRadius: 1,
                                '&:hover': { bgcolor: 'action.hover' }
                              }}
                            />
                          ))}
                        </RadioGroup>
                      </FormControl>
                    </CardContent>
                  </Card>
                ))}

                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button variant="outlined" onClick={() => { setQuiz(null); setAnswers({}); }}>
                    Back to Topics
                  </Button>
                  <Button 
                    variant="contained" 
                    size="large" 
                    onClick={submitQuiz} 
                    disabled={loading || Object.keys(answers).length < quiz.questions?.length}
                  >
                    {loading ? 'Submitting...' : `Submit Quiz (${Object.keys(answers).length}/${quiz.questions?.length})`}
                  </Button>
                </Box>
              </Box>
            )}

            {quizResult && (
              <Box sx={{ mt: 4 }}>
                <Card sx={{ mb: 3, bgcolor: 'success.main', color: 'white' }}>
                  <CardContent sx={{ textAlign: 'center', py: 4 }}>
                    <Typography variant="h4" gutterBottom>🎉 Quiz Completed!</Typography>
                    <Typography variant="h5">Score: {(quizResult.score * 100).toFixed(0)}%</Typography>
                    <Typography variant="body1" sx={{ mt: 1 }}>
                      Correct Answers: {quizResult.correct} / {quizResult.total}
                    </Typography>
                    <Typography variant="h6" sx={{ mt: 2 }}>
                      Your Skill Level: {quizResult.inferredSkill}
                    </Typography>
                  </CardContent>
                </Card>

                <Alert severity="success" sx={{ mb: 3 }}>
                  Your personalized learning path has been generated based on your quiz results!
                </Alert>

                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
                  <Button variant="contained" size="large" onClick={() => setCurrentTab(0)}>
                    Go to Dashboard
                  </Button>
                  <Button variant="outlined" size="large" onClick={() => setCurrentTab(2)}>
                    View Learning Path
                  </Button>
                </Box>
              </Box>
            )}
          </Paper>
        )}

        {/* TAB 2: LEARNING PATH */}
        {currentTab === 2 && (
          <Paper sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>📚 Your Learning Path</Typography>
            {!learningPath ? (
              <Box sx={{ textAlign: 'center', py: 6 }}>
                <Timeline sx={{ fontSize: 80, color: 'primary.main', mb: 2 }} />
                <Typography variant="h5" gutterBottom>No Learning Path Yet</Typography>
                <Typography variant="body1" color="textSecondary" paragraph>
                  Take a quiz or generate a learning path to get started on your learning journey
                </Typography>
                <Box sx={{ mt: 3, display: 'flex', gap: 2, justifyContent: 'center' }}>
                  <Button variant="contained" size="large" onClick={() => setCurrentTab(1)}>Take Quiz</Button>
                  <Button variant="outlined" size="large" onClick={() => setCurrentTab(0)}>Go to Dashboard</Button>
                </Box>
              </Box>
            ) : (
              <Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                  <Typography variant="h5">{learningPath.title}</Typography>
                  <Button variant="contained" color="success" startIcon={<GetApp />} onClick={exportToCSV}>
                    Export CSV
                  </Button>
                </Box>

                {/* Path Stats */}
                <Grid container spacing={2} sx={{ mb: 4 }}>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>Skill Level</Typography>
                        <Typography variant="h6">{learningPath.skillLevel}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>Weekly Hours</Typography>
                        <Typography variant="h6">{learningPath.weeklyHours}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>Estimated Weeks</Typography>
                        <Typography variant="h6">{learningPath.estimatedWeeks}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>Total Hours</Typography>
                        <Typography variant="h6">{learningPath.totalHours}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>

                <Typography variant="h5" gutterBottom sx={{ mt: 4 }}>Learning Modules</Typography>
                <Divider sx={{ mb: 3 }} />

                {learningPath.modules?.map((module, idx) => (
                  <Card key={idx} sx={{ mb: 2 }}>
                    <CardContent>
                      <Typography variant="h6" color="primary.main">{idx + 1}. {module.title}</Typography>
                      <Typography variant="body2" color="textSecondary" paragraph>{module.description}</Typography>
                      <Typography variant="caption" color="textSecondary">⏱️ {module.hours} hours</Typography>
                      {module.resources && module.resources.length > 0 && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" gutterBottom>Resources:</Typography>
                          {module.resources.map((res, rIdx) => (
                            <Chip key={rIdx} label={res} size="small" sx={{ mr: 1, mb: 1 }} />
                          ))}
                        </Box>
                      )}
                    </CardContent>
                  </Card>
                ))}

                <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                  <Button variant="outlined" onClick={() => setCurrentTab(0)}>Back to Dashboard</Button>
                  <Button variant="contained" color="success" startIcon={<GetApp />} onClick={exportToCSV}>
                    Export to CSV
                  </Button>
                </Box>
              </Box>
            )}
          </Paper>
        )}
      </Container>
    </ThemeProvider>
  );
}

export default App;