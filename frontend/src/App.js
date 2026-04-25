import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import CustomerList from './pages/CustomerList';
import CustomerForm from './pages/CustomerForm';
import CustomerView from './pages/CustomerView';
import BulkUpload from './pages/BulkUpload';
import './App.css';

function App() {
  return (
    <div className="app">
      <nav className="navbar">
        <div className="navbar-brand">Customer Management</div>
        <div className="navbar-links">
          <Link to="/">Customers</Link>
          <Link to="/customers/new">Add Customer</Link>
          <Link to="/bulk-upload">Bulk Upload</Link>
        </div>
      </nav>
      <div className="container">
        <Routes>
          <Route path="/" element={<CustomerList />} />
          <Route path="/customers/new" element={<CustomerForm />} />
          <Route path="/customers/edit/:id" element={<CustomerForm />} />
          <Route path="/customers/view/:id" element={<CustomerView />} />
          <Route path="/bulk-upload" element={<BulkUpload />} />
        </Routes>
      </div>
      <ToastContainer position="top-right" autoClose={3000} />
    </div>
  );
}

export default App;