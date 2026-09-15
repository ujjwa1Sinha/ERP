import { useState } from 'react';
import { HiDocumentDownload, HiTable } from 'react-icons/hi';
import api from '../services/api';
import toast from 'react-hot-toast';

/**
 * Reusable export buttons component for PDF and Excel downloads.
 *
 * Props:
 *  - entityType: 'drivers' | 'vehicles' | 'users' | 'branches' | 'trips'
 */
export default function ExportButtons({ entityType }) {
    const [loadingExcel, setLoadingExcel] = useState(false);
    const [loadingPdf, setLoadingPdf] = useState(false);

    const downloadFile = async (format) => {
        const setLoading = format === 'excel' ? setLoadingExcel : setLoadingPdf;
        setLoading(true);
        try {
            const res = await api.get(`/reports/${entityType}/${format}`, {
                responseType: 'blob',
            });

            const ext = format === 'excel' ? 'xlsx' : 'pdf';
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${entityType}_report.${ext}`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
            toast.success(`${format.toUpperCase()} downloaded`);
        } catch {
            /* handled by interceptor */
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', gap: 6 }}>
            <button
                className="btn btn-secondary btn-sm"
                onClick={() => downloadFile('excel')}
                disabled={loadingExcel}
                title="Export as Excel"
                style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}
            >
                <HiTable size={14} />
                {loadingExcel ? 'Exporting...' : 'Excel'}
            </button>
            <button
                className="btn btn-secondary btn-sm"
                onClick={() => downloadFile('pdf')}
                disabled={loadingPdf}
                title="Export as PDF"
                style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}
            >
                <HiDocumentDownload size={14} />
                {loadingPdf ? 'Exporting...' : 'PDF'}
            </button>
        </div>
    );
}
