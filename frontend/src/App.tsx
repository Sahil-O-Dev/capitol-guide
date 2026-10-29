import './App.css'
import { useState } from 'react'


function App() {
    const [status, setStatus] = useState('not connected yet')

    function checkBackend() {
      setStatus('loading');

      fetch('/api/health')
        .then(response => {
          if (!response.ok) {
            throw new Error('Request failed');
          }
          return response.json();
        })
        .then(data => {
          setStatus(data.status);
        })
        .catch(() => {
          setStatus('could not reach backend');
        });
    }

    return (
        <main>
            <h1>Capitol Guide</h1>
            <p>Backend status: {status}</p>
            <button onClick={checkBackend}>
                Check backend
            </button>
        </main>
    )
}

export default App