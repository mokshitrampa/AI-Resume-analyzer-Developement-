import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import { resumeService } from '../../services/resumeService';
import {
  Upload,
  FileText,
  CheckCircle2,
  AlertCircle,
  X,
  ArrowRight,
  ShieldCheck,
  FileCheck2,
  FileCode,
  Sparkles
} from 'lucide-react';

const UploadResumePage = () => {
  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);
  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    setError('');
    if (!selectedFile) return;

    const allowed = ['pdf', 'docx'];
    const ext = selectedFile.name.split('.').pop().toLowerCase();

    if (!allowed.includes(ext)) {
      setError('Unsupported file type. Please upload a PDF or DOCX file.');
      return;
    }

    if (selectedFile.size > 5 * 1024 * 1024) {
      setError('File size exceeds the 5MB limit. Please upload a smaller document.');
      return;
    }

    setFile(selectedFile);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setUploading(true);
    setProgress(30);
    setError('');

    try {
      const formData = new FormData();
      formData.append('file', file);

      setProgress(60);
      const res = await resumeService.uploadResume(formData);
      setProgress(100);
      setSuccess(res);

      // Redirect to review extracted details
      setTimeout(() => {
        navigate(`/student/review?id=${res.resumeId}`);
      }, 1200);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to upload document. Please ensure file is valid.');
      setProgress(0);
    } finally {
      setUploading(false);
    }
  };

  return (
    <DashboardLayout
      title="Upload Resume"
      subtitle="Upload your resume in PDF or DOCX format for automated information extraction and scoring."
    >
      <div className="max-w-3xl mx-auto space-y-8">
        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-600" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-sm flex items-center justify-between">
            <div className="flex items-center gap-3">
              <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600" />
              <span>Resume parsed successfully! Redirecting to review extracted details...</span>
            </div>
            <ArrowRight className="w-4 h-4 animate-bounce" />
          </div>
        )}

        {/* Drop Zone */}
        <div
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          className={`relative border-2 border-dashed rounded-3xl p-8 sm:p-12 text-center transition-all bg-white dark:bg-slate-900 shadow-sm ${
            dragActive
              ? 'border-blue-600 bg-blue-50/50 dark:bg-blue-950/30 scale-[1.01]'
              : 'border-slate-300 dark:border-slate-700 hover:border-slate-400'
          }`}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            className="hidden"
            onChange={(e) => {
              if (e.target.files && e.target.files[0]) {
                validateAndSetFile(e.target.files[0]);
              }
            }}
          />

          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-blue-500/10 via-indigo-500/10 to-purple-500/10 dark:from-blue-500/20 dark:to-purple-500/20 text-blue-600 dark:text-blue-400 flex items-center justify-center mx-auto mb-4">
            <Upload className="w-8 h-8" />
          </div>

          <h3 className="text-lg font-bold text-slate-900 dark:text-white">
            Drag &amp; drop your resume document here
          </h3>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Supports PDF and DOCX files up to 5 MB
          </p>

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="mt-6 inline-flex items-center gap-2 px-6 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-sm font-semibold text-slate-700 dark:text-slate-200 shadow-sm transition-all cursor-pointer"
          >
            Browse Files on Computer
          </button>
        </div>

        {/* Selected File Card */}
        {file && (
          <div className="bg-white dark:bg-slate-900 rounded-2xl p-5 border border-slate-200 dark:border-slate-800 shadow-sm flex items-center justify-between">
            <div className="flex items-center gap-3.5">
              <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 flex items-center justify-center flex-shrink-0">
                <FileText className="w-5 h-5" />
              </div>
              <div className="overflow-hidden">
                <h4 className="font-semibold text-sm text-slate-900 dark:text-white truncate max-w-sm">
                  {file.name}
                </h4>
                <p className="text-xs text-slate-500">
                  {(file.size / (1024 * 1024)).toFixed(2)} MB • {file.name.split('.').pop().toUpperCase()} Document
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setFile(null)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                title="Remove file"
              >
                <X className="w-5 h-5" />
              </button>
              <button
                type="button"
                disabled={uploading}
                onClick={handleUpload}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-md shadow-indigo-500/20 disabled:opacity-60 transition-all cursor-pointer"
              >
                {uploading ? 'Processing & Extracting...' : 'Upload & Parse Details'}
                <Sparkles className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}

        {/* Upload Progress Bar */}
        {uploading && (
          <div className="space-y-2">
            <div className="flex justify-between text-xs font-semibold text-slate-600 dark:text-slate-400">
              <span>Running PDFBox / POI Parsing &amp; NLP Extraction...</span>
              <span>{progress}%</span>
            </div>
            <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-blue-600 to-purple-600 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        )}

        {/* Information & Guidelines */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4">
          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-start gap-3">
            <ShieldCheck className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
            <div>
              <h5 className="font-semibold text-xs text-slate-900 dark:text-white">Secure Storage</h5>
              <p className="text-[11px] text-slate-500 mt-1">
                Uploaded resumes are encrypted, randomized server-side, and accessible only by you.
              </p>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-start gap-3">
            <FileCheck2 className="w-5 h-5 text-indigo-600 flex-shrink-0 mt-0.5" />
            <div>
              <h5 className="font-semibold text-xs text-slate-900 dark:text-white">Format Validation</h5>
              <p className="text-[11px] text-slate-500 mt-1">
                Apache PDFBox and POI validate file MIME signatures to prevent corrupted or malicious files.
              </p>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex items-start gap-3">
            <FileCode className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
            <div>
              <h5 className="font-semibold text-xs text-slate-900 dark:text-white">Review Before Saving</h5>
              <p className="text-[11px] text-slate-500 mt-1">
                You can review, edit, or add skills before finalizing your resume analysis.
              </p>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default UploadResumePage;
