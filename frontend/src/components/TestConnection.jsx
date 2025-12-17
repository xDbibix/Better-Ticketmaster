import { useEffect, useState } from 'react';
import axios from 'axios';

const TestConnection = () => {
  const [status, setStatus] = useState("Testing connection...");

  useEffect(() => {
    // We hit the 'actuator/health' endpoint we just enabled
    axios.get('http://localhost:8080/actuator/health')
      .then(res => {
        if(res.data.status === "UP") {
          setStatus(" onnected to Backend & MongoDB!");
        }
      })
      .catch(err => setStatus("Connection Failed. Is the backend running?"));
  }, []);

  return <div style={{ padding: '20px', border: '1px solid #ccc' }}>{status}</div>;
};

export default TestConnection;