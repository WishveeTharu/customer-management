import React, { useState, useRef } from 'react';
import { toast } from 'react-toastify';
import { bulkUpload } from '../services/api';

function BulkUpload() {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const fileRef = useRef();
  const [progress, setProgress] = useState(0);

  const handleFileDrop = (e) => {
    e.preventDefault();
    const dropped = e.dataTransfer.files[0];
    if (dropped && isExcel(dropped)) setFile(dropped);
    else toast.error('Please upload an Excel file (.xlsx or .xls)');
  };

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (selected && isExcel(selected)) setFile(selected);
    else toast.error('Please upload an Excel file (.xlsx or .xls)');
  };

  const isExcel = (f) =>
    f.name.endsWith('.xlsx') || f.name.endsWith('.xls');

  const handleUpload = async () => {
    if (!file) { toast.warning('Please select a file first'); return; }
    setLoading(true);
    setResult(null);
    try {
      const res = await bulkUpload(file, (progressEvent) => {
        const percent = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        setProgress(percent);
      });
      setResult(res.data);
      if (res.data.errorCount === 0) {
        toast.success(`Successfully processed ${res.data.successCount} customers!`);
      } else {
        toast.warning(`Processed with ${res.data.errorCount} errors`);
      }
    } catch (err) {
      toast.error('Upload failed: ' + (err.message || 'Please try again'));
    } finally {
      setLoading(false);
      setProgress(0);
    }
  };

  const handleReset = () => {
    setFile(null);
    setResult(null);
    if (fileRef.current) fileRef.current.value = '';
  };

  return (
    <div>
      <h1 className="page-title">Bulk Customer Upload</h1>

      <div className="card">
        <div className="section-title">Upload Excel File</div>
        <p style={{ fontSize: '0.9rem', color: '#666', marginBottom: '1rem' }}>
          Upload an Excel file (.xlsx) with columns: <strong>Name</strong>, <strong>Date of Birth</strong>, <strong>NIC Number</strong>
        </p>

        {/* Template download hint */}
        <div style={{
          background: '#ebf5fb', borderRadius: 6,
          padding: '0.8rem 1rem', marginBottom: '1rem',
          fontSize: '0.88rem', color: '#2980b9'
        }}>
          📋 Excel format: Column A = Name | Column B = Date of Birth (yyyy-MM-dd) | Column C = NIC Number
        </div>

        {/* Drop zone */}
        <div className="upload-area"
          onDragOver={e => e.preventDefault()}
          onDrop={handleFileDrop}
          onClick={() => fileRef.current.click()}>
          <div style={{ fontSize: '2.5rem' }}>📁</div>
          {file ? (
            <p style={{ color: '#27ae60', fontWeight: 500 }}>✅ {file.name}</p>
          ) : (
            <>
              <p><strong>Click to browse</strong> or drag & drop your Excel file here</p>
              <p>Supports .xlsx and .xls files up to 200MB (1,000,000+ rows)</p>
            </>
          )}
        </div>

        {loading && (
          <div style={{ marginTop: '1rem' }}>
            <div style={{ fontSize: '0.9rem', color: '#555', marginBottom: '0.4rem' }}>
              {progress < 100 ? `Uploading... ${progress}%` : 'Processing records... please wait'}
            </div>
            <div style={{ background: '#eee', borderRadius: 4, height: 8 }}>
              <div style={{
                background: '#3498db', height: 8, borderRadius: 4,
                width: `${progress}%`, transition: 'width 0.3s'
              }} />
            </div>
          </div>
        )}

        <input ref={fileRef} type="file"
          accept=".xlsx,.xls" style={{ display: 'none' }}
          onChange={handleFileChange} />

        <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
          <button className="btn btn-primary" onClick={handleUpload} disabled={loading || !file}>
            {loading ? 'Uploading...' : 'Upload & Process'}
          </button>
          {(file || result) && (
            <button className="btn btn-secondary" onClick={handleReset}>Reset</button>
          )}
        </div>
      </div>

      {/* Results */}
      {result && (
        <div className="card">
          <div className="section-title">Upload Results</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginBottom: '1rem' }}>
            <div style={{ background: '#f8f9fa', borderRadius: 6, padding: '1rem', textAlign: 'center' }}>
              <div style={{ fontSize: '1.8rem', fontWeight: 700, color: '#2c3e50' }}>{result.totalRows}</div>
              <div style={{ fontSize: '0.85rem', color: '#888' }}>Total Rows</div>
            </div>
            <div style={{ background: '#eafaf1', borderRadius: 6, padding: '1rem', textAlign: 'center' }}>
              <div style={{ fontSize: '1.8rem', fontWeight: 700, color: '#27ae60' }}>{result.successCount}</div>
              <div style={{ fontSize: '0.85rem', color: '#27ae60' }}>Successful</div>
            </div>
            <div style={{ background: '#fdedec', borderRadius: 6, padding: '1rem', textAlign: 'center' }}>
              <div style={{ fontSize: '1.8rem', fontWeight: 700, color: '#e74c3c' }}>{result.errorCount}</div>
              <div style={{ fontSize: '0.85rem', color: '#e74c3c' }}>Errors</div>
            </div>
          </div>

          {result.errors.length > 0 && (
            <div>
              <div style={{ fontWeight: 500, marginBottom: '0.5rem', color: '#e74c3c' }}>Error Details:</div>
              <div style={{
                background: '#fdedec', borderRadius: 6, padding: '1rem',
                maxHeight: '200px', overflowY: 'auto', fontSize: '0.85rem'
              }}>
                {result.errors.map((err, i) => (
                  <div key={i} style={{ padding: '0.2rem 0', borderBottom: '1px solid #fad7d3' }}>
                    {err}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default BulkUpload;