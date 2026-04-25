import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getCustomerById } from '../services/api';

function CustomerView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    loadCustomer();
  }, [id]);

  const loadCustomer = async () => {
    try {
      const res = await getCustomerById(id);
      setCustomer(res.data);
    } catch (err) {
      toast.error('Failed to load customer');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!customer) return <div className="empty">Customer not found</div>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 className="page-title" style={{ margin: 0 }}>{customer.name}</h1>
        <div className="actions">
          <button className="btn btn-warning"
            onClick={() => navigate(`/customers/edit/${id}`)}>Edit</button>
          <button className="btn btn-secondary"
            onClick={() => navigate('/')}>Back</button>
        </div>
      </div>

      {/* Basic Info */}
      <div className="card">
        <div className="section-title">Basic Information</div>
        <div className="detail-grid">
          <div className="detail-item">
            <label>Full Name</label>
            <p>{customer.name}</p>
          </div>
          <div className="detail-item">
            <label>NIC Number</label>
            <p><span className="badge badge-blue">{customer.nicNumber}</span></p>
          </div>
          <div className="detail-item">
            <label>Date of Birth</label>
            <p>{customer.dateOfBirth}</p>
          </div>
          <div className="detail-item">
            <label>Created At</label>
            <p>{new Date(customer.createdAt).toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* Mobile Numbers */}
      <div className="card">
        <div className="section-title">Mobile Numbers</div>
        {customer.mobileNumbers.length === 0 ? (
          <p style={{ color: '#aaa' }}>No mobile numbers</p>
        ) : (
          <div className="tag-list">
            {customer.mobileNumbers.map((m, i) => (
              <span key={i} className="tag">{m}</span>
            ))}
          </div>
        )}
      </div>

      {/* Addresses */}
      <div className="card">
        <div className="section-title">Addresses</div>
        {customer.addresses.length === 0 ? (
          <p style={{ color: '#aaa' }}>No addresses</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {customer.addresses.map((addr, i) => (
              <div key={i} style={{
                border: '1px solid #eee', borderRadius: 6,
                padding: '1rem', background: '#fafafa'
              }}>
                <div style={{ fontWeight: 600, marginBottom: '0.5rem', color: '#2c3e50' }}>
                  Address {i + 1}
                </div>
                <div className="detail-grid">
                  <div className="detail-item">
                    <label>Address Line 1</label>
                    <p>{addr.addressLine1}</p>
                  </div>
                  <div className="detail-item">
                    <label>Address Line 2</label>
                    <p>{addr.addressLine2 || '—'}</p>
                  </div>
                  <div className="detail-item">
                    <label>City</label>
                    <p>{addr.cityName}</p>
                  </div>
                  <div className="detail-item">
                    <label>Country</label>
                    <p>{addr.countryName}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Family Members */}
      <div className="card">
        <div className="section-title">Family Members</div>
        {customer.familyMembers.length === 0 ? (
          <p style={{ color: '#aaa' }}>No family members linked</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px,1fr))', gap: '0.6rem' }}>
            {customer.familyMembers.map(fm => (
              <div key={fm.id} style={{
                border: '1px solid #eee', borderRadius: 6,
                padding: '0.7rem 1rem', background: '#fafafa',
                cursor: 'pointer'
              }}
                onClick={() => navigate(`/customers/view/${fm.id}`)}>
                <div style={{ fontWeight: 500 }}>{fm.name}</div>
                <div style={{ fontSize: '0.82rem', color: '#888' }}>{fm.nicNumber}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default CustomerView;