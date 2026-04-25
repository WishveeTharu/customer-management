import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getCustomers, deleteCustomer } from '../services/api';

function CustomerList() {
  const [customers, setCustomers] = useState([]);
  const [search, setSearch]       = useState('');
  const [page, setPage]           = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading]     = useState(false);
  const navigate = useNavigate();

  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getCustomers(search, page, 10);
      setCustomers(res.data.content);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
    } catch (err) {
      toast.error('Failed to load customers');
    } finally {
      setLoading(false);
    }
  }, [search, page]);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchCustomers();
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete customer "${name}"?`)) return;
    try {
      await deleteCustomer(id);
      toast.success('Customer deleted successfully');
      fetchCustomers();
    } catch (err) {
      toast.error('Failed to delete customer');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 className="page-title" style={{ margin: 0 }}>Customers</h1>
        <Link to="/customers/new" className="btn btn-primary">+ Add Customer</Link>
      </div>

      <div className="card">
        <form className="search-bar" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search by name or NIC..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
          {search && (
            <button type="button" className="btn btn-secondary"
              onClick={() => { setSearch(''); setPage(0); }}>
              Clear
            </button>
          )}
        </form>

        <div style={{ fontSize: '0.85rem', color: '#888', marginBottom: '0.8rem' }}>
          {totalElements} customer(s) found
        </div>

        {loading ? (
          <div className="loading">Loading...</div>
        ) : customers.length === 0 ? (
          <div className="empty">No customers found</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>NIC Number</th>
                <th>Date of Birth</th>
                <th>Mobile</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((c, index) => (
                <tr key={c.id}>
                  <td>{page * 10 + index + 1}</td>
                  <td>
                    <strong>{c.name}</strong>
                  </td>
                  <td><span className="badge badge-blue">{c.nicNumber}</span></td>
                  <td>{c.dateOfBirth}</td>
                  <td>{c.primaryMobile || <span style={{ color: '#ccc' }}>—</span>}</td>
                  <td>
                    <div className="actions">
                      <button className="btn btn-secondary"
                        onClick={() => navigate(`/customers/view/${c.id}`)}>
                        View
                      </button>
                      <button className="btn btn-warning"
                        onClick={() => navigate(`/customers/edit/${c.id}`)}>
                        Edit
                      </button>
                      <button className="btn btn-danger"
                        onClick={() => handleDelete(c.id, c.name)}>
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>
              &laquo; Prev
            </button>
            {[...Array(totalPages)].map((_, i) => (
              <button key={i} className={page === i ? 'active' : ''}
                onClick={() => setPage(i)}>
                {i + 1}
              </button>
            ))}
            <button disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}>
              Next &raquo;
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default CustomerList;