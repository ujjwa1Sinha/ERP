import { useState, useRef } from 'react';
import { HiX, HiUpload, HiDocumentText, HiDownload, HiExclamation, HiCheckCircle, HiExclamationCircle } from 'react-icons/hi';
import api from '../services/api';
import toast from 'react-hot-toast';

/**
 * Reusable modal for bulk-importing data from an .xlsx file.
 * 
 * Props:
 *  - entityType: 'drivers' | 'vehicles' | 'users' | 'branches'
 *  - entityLabel: Display name, e.g. "Drivers"
 *  - isOpen: boolean
 *  - onClose: function
 *  - onSuccess: function (called after successful import)
 */
export default function BulkImportModal({ entityType, entityLabel, isOpen, onClose, onSuccess }) {
    const [file, setFile] = useState(null);
    const [fileError, setFileError] = useState('');
    const [importing, setImporting] = useState(false);
    const [result, setResult] = useState(null);
    const [downloading, setDownloading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const fileInputRef = useRef(null);

    if (!isOpen) return null;

    const handleFileChange = (e) => {
        const selected = e.target.files[0];
        setFileError('');
        setResult(null);
        if (!selected) { setFile(null); return; }

        if (!selected.name.toLowerCase().endsWith('.xlsx')) {
            setFileError('Only .xlsx files are accepted');
            setFile(null);
            return;
        }
        if (selected.size > 5 * 1024 * 1024) {
            setFileError('File exceeds 5MB limit');
            setFile(null);
            return;
        }
        setFile(selected);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        const dropped = e.dataTransfer.files[0];
        if (dropped) {
            const fakeEvent = { target: { files: [dropped] } };
            handleFileChange(fakeEvent);
        }
    };

    const handleDragOver = (e) => e.preventDefault();

    const handleImport = async () => {
        if (!file) return;
        setImporting(true);
        setUploadProgress(0);
        setResult(null);
        const formData = new FormData();
        formData.append('file', file);

        try {
            const res = await api.post(`/import/${entityType}`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                onUploadProgress: (progressEvent) => {
                    if (progressEvent.total) {
                        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                        setUploadProgress(percent);
                    }
                }
            });
            const data = res.data.data;
            setResult(data);
            if (data.successCount > 0) {
                toast.success(`${data.successCount} ${entityLabel.toLowerCase()} imported successfully`);
                if (onSuccess) onSuccess();
            }
            if (data.errorCount > 0) {
                toast.error(`${data.errorCount} rows had errors`);
            }
        } catch (err) {
            /* handled by interceptor */
        } finally {
            setImporting(false);
        }
    };

    const downloadTemplate = async () => {
        setDownloading(true);
        try {
            const res = await api.get(`/import/${entityType}/template`, { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${entityType}_template.xlsx`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch {
            /* handled */
        } finally {
            setDownloading(false);
        }
    };

    const handleClose = () => {
        setFile(null);
        setFileError('');
        setResult(null);
        onClose();
    };

    const formatFileSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    };

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 560 }}>
                <div className="modal-header">
                    <h3>Import {entityLabel}</h3>
                    <button className="modal-close" onClick={handleClose}><HiX /></button>
                </div>
                <div className="modal-body">
                    {/* Template download */}
                    <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
                        <button
                            className="btn btn-secondary btn-sm"
                            onClick={downloadTemplate}
                            disabled={downloading}
                            style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
                        >
                            <HiDownload size={14} />
                            {downloading ? 'Downloading...' : 'Download Template'}
                        </button>
                        <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>
                            Use this template to format your data correctly
                        </span>
                    </div>

                    {/* Drop zone */}
                    <div
                        className={`file-upload-area ${fileError ? 'file-upload-error' : ''} ${file ? 'file-upload-filled' : ''}`}
                        onDrop={handleDrop}
                        onDragOver={handleDragOver}
                        style={{ marginBottom: 12 }}
                    >
                        {!file ? (
                            <label className="file-upload-label" htmlFor="bulk-import-file-input">
                                <HiUpload size={28} className="file-upload-icon" />
                                <span className="file-upload-text">
                                    Drag & drop or click to upload
                                </span>
                                <span className="file-upload-hint">.xlsx files only, max 5MB</span>
                                <input
                                    ref={fileInputRef}
                                    id="bulk-import-file-input"
                                    type="file"
                                    accept=".xlsx"
                                    onChange={handleFileChange}
                                    className="file-upload-input"
                                />
                            </label>
                        ) : (
                            <div className="file-upload-preview">
                                <div className="file-upload-file-info">
                                    <HiDocumentText size={20} style={{ color: 'var(--green-500)', flexShrink: 0 }} />
                                    <div className="file-upload-details">
                                        <span className="file-upload-name">{file.name}</span>
                                        <span className="file-upload-size">{formatFileSize(file.size)}</span>
                                    </div>
                                </div>
                                <button
                                    type="button"
                                    className="file-upload-remove"
                                    onClick={() => { setFile(null); setResult(null); if (fileInputRef.current) fileInputRef.current.value = ''; }}
                                    title="Remove file"
                                >
                                    <HiX size={16} />
                                </button>
                            </div>
                        )}
                    </div>
                    {fileError && (
                        <span className="file-upload-error-text"><HiExclamation size={14} /> {fileError}</span>
                    )}

                    {/* Progress Bar (Animated) */}
                    {importing && (
                        <div style={{ marginTop: 16 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}>
                                <strong style={{ color: 'var(--blue-500)' }}>
                                    {uploadProgress === 100 || uploadProgress === 0 ? 'Processing data...' : `Uploading file...`}
                                </strong>
                                <span style={{ color: 'var(--gray-500)', fontWeight: 500 }}>
                                    {uploadProgress === 0 ? 'Loading...' : `${uploadProgress}%`}
                                </span>
                            </div>
                            <div style={{ width: '100%', height: 8, background: 'rgba(0,0,0,0.1)', borderRadius: 4, overflow: 'hidden', position: 'relative' }}>
                                <div style={{
                                    height: '100%',
                                    width: uploadProgress > 0 ? `${uploadProgress}%` : '50%',
                                    background: 'linear-gradient(90deg, var(--blue-400), var(--blue-600))',
                                    transition: 'width 0.3s ease',
                                    animation: uploadProgress === 0 || uploadProgress === 100 ? 'pulse 1.5s infinite alternate' : 'none',
                                    borderRadius: 4
                                }} />
                            </div>
                        </div>
                    )}

                    {/* Import result */}
                    {result && (
                        <div style={{
                            marginTop: 16, padding: 16, borderRadius: 8,
                            background: result.errorCount === 0 ? 'var(--green-50, #f0fdf4)' : 'var(--amber-50, #fffbeb)',
                            border: `1px solid ${result.errorCount === 0 ? 'var(--green-200, #bbf7d0)' : 'var(--amber-200, #fde68a)'}`
                        }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                {result.errorCount === 0
                                    ? <HiCheckCircle size={20} style={{ color: 'var(--green-500)' }} />
                                    : <HiExclamationCircle size={20} style={{ color: 'var(--amber-500)' }} />
                                }
                                <strong style={{ fontSize: 14 }}>Import Results</strong>
                            </div>
                            <div style={{ display: 'flex', gap: 20, fontSize: 13 }}>
                                <span>Total: <strong style={{ color: 'var(--gray-900)' }}>{result.totalRows}</strong></span>
                                <span style={{ color: 'var(--green-500, #22c55e)' }}>
                                    Success: <strong>{result.successCount}</strong>
                                </span>
                                <span style={{ color: result.errorCount > 0 ? 'var(--red-500, #ef4444)' : 'var(--gray-500)' }}>
                                    Errors: <strong>{result.errorCount}</strong>
                                </span>
                            </div>

                            {result.errors && result.errors.length > 0 && (
                                <div style={{ marginTop: 12, maxHeight: 250, overflow: 'auto', background: 'var(--glass-card)', borderRadius: 6, padding: 0, border: '1px solid var(--glass-border)' }}>
                                    <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse', textAlign: 'left' }}>
                                        <thead>
                                            <tr style={{ background: 'rgba(0,0,0,0.1)' }}>
                                                <th style={{ padding: '8px 12px', fontWeight: 700, color: 'var(--gray-800)' }}>Row</th>
                                                <th style={{ padding: '8px 12px', fontWeight: 700, color: 'var(--gray-800)' }}>Error Message</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {result.errors.map((err, idx) => (
                                                <tr key={idx} style={{ borderTop: '1px solid rgba(0,0,0,0.05)', background: 'rgba(239, 68, 68, 0.1)' }}>
                                                    <td style={{ padding: '8px 12px', whiteSpace: 'nowrap', fontWeight: 700, color: 'var(--red-500, #ef4444)' }}>Row {err.row}</td>
                                                    <td style={{ padding: '8px 12px', color: 'var(--red-500, #ef4444)', fontWeight: 600 }}>{err.message}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* Actions */}
                <div style={{ padding: '12px 20px', display: 'flex', justifyContent: 'flex-end', gap: 8, borderTop: '1px solid var(--gray-100)' }}>
                    <button className="btn btn-secondary" onClick={handleClose}>
                        {result ? 'Close' : 'Cancel'}
                    </button>
                    {!result && (
                        <button
                            className="btn btn-primary"
                            onClick={handleImport}
                            disabled={!file || importing || !!fileError}
                        >
                            {importing ? 'Importing...' : `Import ${entityLabel}`}
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
