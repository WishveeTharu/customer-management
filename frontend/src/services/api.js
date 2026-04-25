import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

// const api = axios.create({
//   baseURL: BASE_URL,
//   headers: { 'Content-Type': 'application/json' },
// });

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 3600000,
});

// ── Customer APIs ─────────────────────────────────────────────────────────────

export const getCustomers = (search = '', page = 0, size = 10) =>
  api.get('/customers', { params: { search, page, size } });

export const getCustomerById = (id) =>
  api.get(`/customers/${id}`);

export const createCustomer = (data) =>
  api.post('/customers', data);

export const updateCustomer = (id, data) =>
  api.put(`/customers/${id}`, data);

export const deleteCustomer = (id) =>
  api.delete(`/customers/${id}`);

// export const bulkUpload = (file) => {
//   const formData = new FormData();
//   formData.append('file', file);
//   return api.post('/customers/bulk-upload', formData, {
//     headers: { 'Content-Type': 'multipart/form-data' },
//   });
// };

export const bulkUpload = (file, onUploadProgress) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/customers/bulk-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 0,
    onUploadProgress,
  });
};

// ── Master Data APIs ──────────────────────────────────────────────────────────

export const getCountries = () =>
  api.get('/master/countries');

export const getCitiesByCountry = (countryId) =>
  api.get('/master/cities', { params: { countryId } });

export default api;