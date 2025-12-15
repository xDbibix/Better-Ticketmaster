import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <Router>
      <nav> {/* Basic Navbar for testing */}
        <a href="/">Home</a> | <a href="/search">Search</a>
      </nav>
      
      <Routes>
        <Route path="/" element={<h1>Welcome to BetterTicketMaster</h1>} />
        <Route path="/search" element={<h1>Search Events Here</h1>} />
        <Route path="/event/:id" element={<h1>Seat Selection Map</h1>} />
      </Routes>
    </Router>
  );
}

export default App;