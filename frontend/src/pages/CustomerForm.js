import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import DatePicker from 'react-datepicker';
import { toast } from 'react-toastify';
import {
  getCustomerById, createCustomer, updateCustomer,
  getCountries, getCitiesByCountry, getCustomers
} from '../services/api';

function CustomerForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    name: '', nicNumber: '', dateOfBirth: null,
    mobileNumbers: [], addresses: [], familyMemberIds: []
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [countries, setCountries] = useState([]);
  const [citiesMap, setCitiesMap] = useState({});
  const [allCustomers, setAllCustomers] = useState([]);
  const [mobileInput, setMobileInput] = useState('');

  useEffect(() => {
    loadMasterData();
    if (isEdit) loadCustomer();
  }, [id]);

  const loadMasterData = async () => {
    try {
      const [cRes, custRes] = await Promise.all([
        getCountries(),
        getCustomers('', 0, 1000)
      ]);
      setCountries(cRes.data);
      setAllCustomers(custRes.data.content.filter(c => String(c.id) !== String(id)));
    } catch (err) {
      toast.error('Failed to load master data');
    }
  };

  const loadCustomer = async () => {
    try {
      const res = await getCustomerById(id);
      const c = res.data;
      setForm({
        name: c.name,
        nicNumber: c.nicNumber,
        dateOfBirth: c.dateOfBirth ? new Date(c.dateOfBirth) : null,
        mobileNumbers: c.mobileNumbers || [],
        addresses: c.addresses.map(a => ({
          addressLine1: a.addressLine1,
          addressLine2: a.addressLine2 || '',
          cityId: a.cityId,
          countryId: a.countryId
        })),
        familyMemberIds: c.familyMembers.map(f => f.id)
      });
      // Load cities for each address country
      const uniqueCountries = [...new Set(c.addresses.map(a => a.countryId))];
      for (const cId of uniqueCountries) {
        await loadCities(cId);
      }
    } catch (err) {
      toast.error('Failed to load customer');
    }
  };

  // const loadCities = async (countryId) => {
  //   if (!countryId || citiesMap[countryId]) return;
  //   try {
  //     const res = await getCitiesByCountry(countryId);
  //     setCitiesMap(prev => ({ ...prev, [countryId]: res.data }));
  //   } catch (err) { }
  // };

  const loadCities = async (countryId) => {
    if (!countryId) return;
    try {
      const res = await getCitiesByCountry(countryId);
      setCitiesMap(prev => ({ ...prev, [countryId]: res.data }));
    } catch (err) {
      toast.error('Failed to load cities');
    }
  };

  // const validate = () => {
  //   const errs = {};
  //   if (!form.name.trim())       errs.name = 'Name is required';
  //   if (!form.nicNumber.trim())  errs.nicNumber = 'NIC number is required';
  //   if (!form.dateOfBirth)       errs.dateOfBirth = 'Date of birth is required';
  //   setErrors(errs);
  //   return Object.keys(errs).length === 0;
  // };

  const validate = () => {
    const errs = {};
    if (!form.name.trim()) errs.name = 'Name is required';
    if (!form.nicNumber.trim()) errs.nicNumber = 'NIC number is required';
    else if (!/^\d{12}$/.test(form.nicNumber.trim())) errs.nicNumber = 'NIC must be exactly 12 digits';
    if (!form.dateOfBirth) errs.dateOfBirth = 'Date of birth is required';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const payload = {
        ...form,
        dateOfBirth: form.dateOfBirth
          ? form.dateOfBirth.toISOString().split('T')[0] : null,
      };
      if (isEdit) {
        await updateCustomer(id, payload);
        toast.success('Customer updated successfully!');
      } else {
        await createCustomer(payload);
        toast.success('Customer created successfully!');
      }
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || 'Something went wrong';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Mobile numbers ────────────────────────────────────────────────────────

  // const addMobile = () => {
  //   if (!mobileInput.trim()) return;
  //   setForm(f => ({ ...f, mobileNumbers: [...f.mobileNumbers, mobileInput.trim()] }));
  //   setMobileInput('');
  // };

  const addMobile = () => {
    if (!mobileInput.trim()) return;
    if (!/^\d{10}$/.test(mobileInput.trim())) {
      toast.error('Mobile number must be exactly 10 digits');
      return;
    }
    setForm(f => ({ ...f, mobileNumbers: [...f.mobileNumbers, mobileInput.trim()] }));
    setMobileInput('');
  };

  const removeMobile = (index) => {
    setForm(f => ({ ...f, mobileNumbers: f.mobileNumbers.filter((_, i) => i !== index) }));
  };

  // ── Addresses ─────────────────────────────────────────────────────────────

  const addAddress = () => {
    setForm(f => ({
      ...f,
      addresses: [...f.addresses, { addressLine1: '', addressLine2: '', cityId: '', countryId: '' }]
    }));
  };

  const removeAddress = (index) => {
    setForm(f => ({ ...f, addresses: f.addresses.filter((_, i) => i !== index) }));
  };

  // const updateAddress = (index, field, value) => {
  //   setForm(f => {
  //     const addresses = [...f.addresses];
  //     addresses[index] = { ...addresses[index], [field]: value };
  //     if (field === 'countryId') {
  //       addresses[index].cityId = '';
  //       loadCities(value);
  //     }
  //     return { ...f, addresses };
  //   });
  // };

  const updateAddress = (index, field, value) => {
    setForm(f => {
      const addresses = [...f.addresses];
      addresses[index] = { ...addresses[index], [field]: value };
      if (field === 'countryId') {
        addresses[index].cityId = '';
        loadCities(value);
      }
      return { ...f, addresses };
    });
  };

  // ── Family members ────────────────────────────────────────────────────────

  const toggleFamily = (custId) => {
    setForm(f => {
      const ids = f.familyMemberIds.includes(custId)
        ? f.familyMemberIds.filter(i => i !== custId)
        : [...f.familyMemberIds, custId];
      return { ...f, familyMemberIds: ids };
    });
  };

  return (
    <div>
      <h1 className="page-title">{isEdit ? 'Edit Customer' : 'Add Customer'}</h1>

      <form onSubmit={handleSubmit}>
        <div className="card">
          <div className="section-title">Basic Information</div>
          <div className="form-row">
            <div className="form-group">
              <label>Name *</label>
              <input type="text" value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                placeholder="Full name" />
              {errors.name && <div className="error-text">{errors.name}</div>}
            </div>
            <div className="form-group">
              <label>NIC Number *</label>
              <input type="text" value={form.nicNumber}
                onChange={e => setForm(f => ({ ...f, nicNumber: e.target.value }))}
                placeholder="NIC number" />
              {errors.nicNumber && <div className="error-text">{errors.nicNumber}</div>}
            </div>
          </div>
          <div className="form-group" style={{ maxWidth: '300px' }}>
            <label>Date of Birth *</label>
            <DatePicker
              selected={form.dateOfBirth}
              onChange={date => setForm(f => ({ ...f, dateOfBirth: date }))}
              dateFormat="yyyy-MM-dd"
              placeholderText="Select date of birth"
              showYearDropdown
              scrollableYearDropdown
              yearDropdownItemNumber={80}
              maxDate={new Date()}
            />
            {errors.dateOfBirth && <div className="error-text">{errors.dateOfBirth}</div>}
          </div>
        </div>

        {/* Mobile Numbers */}
        <div className="card">
          <div className="section-title">Mobile Numbers</div>
          <div className="tag-input-row">
            <input type="text" value={mobileInput}
              onChange={e => setMobileInput(e.target.value)}
              placeholder="Enter mobile number"
              onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), addMobile())}
            />
            <button type="button" className="btn btn-primary" onClick={addMobile}>Add</button>
          </div>
          <div className="tag-list">
            {form.mobileNumbers.map((m, i) => (
              <span key={i} className="tag">
                {m}
                <button type="button" onClick={() => removeMobile(i)}>×</button>
              </span>
            ))}
          </div>
        </div>

        {/* Addresses */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div className="section-title" style={{ margin: 0 }}>Addresses</div>
            <button type="button" className="btn btn-primary" onClick={addAddress}>+ Add Address</button>
          </div>
          {form.addresses.map((addr, i) => (
            <div key={i} style={{ border: '1px solid #eee', borderRadius: 6, padding: '1rem', marginTop: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.8rem' }}>
                <strong style={{ fontSize: '0.9rem' }}>Address {i + 1}</strong>
                <button type="button" className="btn btn-danger"
                  onClick={() => removeAddress(i)}>Remove</button>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Country *</label>
                  <select value={addr.countryId}
                    onChange={e => updateAddress(i, 'countryId', e.target.value)}>
                    <option value="">Select country</option>
                    {countries.map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>City *</label>
                  <select value={addr.cityId}
                    onChange={e => updateAddress(i, 'cityId', e.target.value)}
                    disabled={!addr.countryId}>
                    <option value="">Select city</option>
                    {(citiesMap[addr.countryId] || []).map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Address Line 1 *</label>
                <input type="text" value={addr.addressLine1}
                  onChange={e => updateAddress(i, 'addressLine1', e.target.value)}
                  placeholder="Address line 1" />
              </div>
              <div className="form-group">
                <label>Address Line 2</label>
                <input type="text" value={addr.addressLine2}
                  onChange={e => updateAddress(i, 'addressLine2', e.target.value)}
                  placeholder="Address line 2 (optional)" />
              </div>
            </div>
          ))}
          {form.addresses.length === 0 && (
            <p style={{ color: '#aaa', marginTop: '0.8rem', fontSize: '0.9rem' }}>No addresses added</p>
          )}
        </div>

        {/* Family Members */}
        <div className="card">
          <div className="section-title">Family Members</div>
          {allCustomers.length === 0 ? (
            <p style={{ color: '#aaa', fontSize: '0.9rem' }}>No other customers available</p>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px,1fr))', gap: '0.6rem' }}>
              {allCustomers.map(c => (
                <label key={c.id} style={{
                  display: 'flex', alignItems: 'center', gap: '0.6rem',
                  padding: '0.5rem 0.8rem', border: '1px solid #eee',
                  borderRadius: 6, cursor: 'pointer',
                  background: form.familyMemberIds.includes(c.id) ? '#ebf5fb' : 'white'
                }}>
                  <input type="checkbox"
                    checked={form.familyMemberIds.includes(c.id)}
                    onChange={() => toggleFamily(c.id)} />
                  <div>
                    <div style={{ fontWeight: 500, fontSize: '0.9rem' }}>{c.name}</div>
                    <div style={{ fontSize: '0.78rem', color: '#888' }}>{c.nicNumber}</div>
                  </div>
                </label>
              ))}
            </div>
          )}
        </div>

        {/* Submit */}
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button type="submit" className="btn btn-success" disabled={loading}>
            {loading ? 'Saving...' : isEdit ? 'Update Customer' : 'Create Customer'}
          </button>
          <button type="button" className="btn btn-secondary"
            onClick={() => navigate('/')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}

export default CustomerForm;