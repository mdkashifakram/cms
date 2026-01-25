// ChatbotWidget.js

import React, { useState } from 'react';
import ChatBot from 'react-simple-chatbot';
import axios from 'axios';
import logger from '../../utils/logger'; // CMS-010: Secure logging

const ChatbotWidget = () => {
  const [chatHistory, setChatHistory] = useState([]);

  const handleEnd = async (steps) => {
    const userMessage = steps[steps.length - 1]?.message;

    if (!userMessage) return;

    try {
      // Sending user's message to the backend API
      const response = await axios.post('http://127.0.0.1:5000/chat', {
        message: userMessage,
      });
      const botMessage = response.data.response;

      // Updating chat history
      setChatHistory((prevChat) => [
        ...prevChat,
        { type: 'user', message: userMessage },
        { type: 'bot', message: botMessage || 'Sorry, I could not fetch a response.' },
      ]);
    } catch (error) {
      logger.error('Error fetching chatbot response:', error.message);

      setChatHistory((prevChat) => [
        ...prevChat,
        { type: 'user', message: userMessage },
        { type: 'bot', message: 'I am currently unable to assist. Please try again later.' },
      ]);
    }
  };

  const steps = [
    {
      id: '1',
      message: 'Hi, how can I assist you today?',
      trigger: 'user-input',
    },
    {
      id: 'user-input',
      user: true,
      trigger: 'bot-wait',
    },
    {
      id: 'bot-wait',
      message: 'Thank you! Please wait while I fetch an answer.',
      trigger: 'bot-response',
    },
    {
      id: 'bot-response',
      message: ({ steps }) => {
        // Get the last user message
        const userMessage = steps['user-input']?.value;
        return `You said: "${userMessage}". Fetching response...`;
      },
      end: true, // End the chatbot flow to avoid repetition
    },
  ];

  return (
    <div style={{ width: '400px', margin: '0 auto' }}>
      <ChatBot
        steps={steps}
        handleEnd={handleEnd}
        headerTitle="Chatbot Assistant"
        placeholder="Type your message here..."
        botAvatar="/path-to-bot-avatar.png" // Replace with your bot avatar
        userAvatar="/path-to-user-avatar.png" // Replace with your user avatar
      />

      {/* Chat History */}
      <div style={{ marginTop: '20px' }}>
        <h3>Chat History</h3>
        {chatHistory.map((message, index) => (
          <div
            key={index}
            style={{
              display: 'flex',
              justifyContent: message.type === 'user' ? 'flex-end' : 'flex-start',
              marginBottom: '10px',
            }}
          >
            <div
              style={{
                maxWidth: '70%',
                padding: '10px',
                borderRadius: '10px',
                backgroundColor: message.type === 'user' ? '#daf1da' : '#f1f0f0',
              }}
            >
              {message.message}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ChatbotWidget;
