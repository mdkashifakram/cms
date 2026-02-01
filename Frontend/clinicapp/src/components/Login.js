import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Login.css';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api'; // Security Remediation: Use API config

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(''); // State to handle error messages
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();

        // Prepare the data to be sent to the backend
        const loginData = {
            username: email,
            password: password
        };

        try {
            // Security Remediation: Use API config instead of hardcoded URL
            // HttpOnly cookies are used for authentication
            const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.LOGIN}`, {
                method: 'POST',
                headers: API_CONFIG.HEADERS,
                credentials: API_CONFIG.CREDENTIALS,
                body: JSON.stringify(loginData),
            });

            if (!response.ok) {
                throw new Error('Login failed. Please check your credentials.');
            }

            // Parse the response
            const result = await response.json();

            // Security Remediation: JWT is stored in HttpOnly cookie by backend
            // NO localStorage storage - prevents XSS attacks from stealing tokens
            logger.log('Login successful');

            // Redirect to the dashboard
            navigate('/dashboard');
        } catch (error) {
            // Handle errors, such as wrong credentials
            setError(error.message);
            logger.error('Login error:', error.message);
        }
    };

    return (
        <div className="login-container">
            <div className="login-form-container ">
                <div className='position'>
                    <div className='rectangle-box'></div>
                    <div className='image-container'>
                        <img src={require('../assets/12e40b4196d2b930.png')} alt="Doctor" className="login-image" />
                    </div>
                </div>


                <form className="login-form" onSubmit={handleLogin}>
                    <h2>Welcome, back!</h2>
                    {error && <p className="error-message">{error}</p>} {/* Display error message */}
                    <input
                        type="text"
                        placeholder="Enter your username"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Enter your password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit">Sign In</button>
                    <div className="login-footer">
                        <a href="#">Forgot Password?</a>
                    </div>
                </form>

            </div>
        </div>
    );
};

export default Login;
