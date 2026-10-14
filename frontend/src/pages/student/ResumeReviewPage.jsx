import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import SkillBadge from '../../components/SkillBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import { resumeService } from '../../services/resumeService';
import {
  FileText,
  User,
  Mail,
  Phone,
  MapPin,
  GraduationCap,
  Calendar,
  Sparkles,
  Save,
  ArrowRight,
  Plus,
  AlertCircle,
  CheckCircle2
} from 'lucide-react';

const ResumeReviewPage = () => {
  const [searchParams] = useSearchParams();
  const resumeId = searchParams.get('id');
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [newSkillInput, setNewSkillInput] = useState('');

  const [formData, setFormData] = useState({
    candidateName: '',
    email: '',
    phone: '',
    location: '',
    degree: '',
    institution: '',
    graduationYear: '',
    cgpa: '',
    technicalSkills: [],
  });

  useEffect(() => {
    if (!resumeId) {
      navigate('/student/upload');
      return;
    }
    loadExtractedData();
  }, [resumeId]);

  const loadExtractedData = async () => {
    try {
      setLoading(true);
      const data = await resumeService.getExtractedData(resumeId);
      setFormData({
        candidateName: data.candidateName || '',
        email: data.email || '',
        phone: data.phone || '',
        location: data.location || '',
        degree: data.degree || '',
        institution: data.institution || '',
        graduationYear: data.graduationYear || '',
        cgpa: data.cgpa || '',
        technicalSkills: data.technicalSkills || [],
      });
    } catch (err) {
      console.error(err);
      setError('Could not load extracted resume details.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddSkill = (e) => {
    e.preventDefault();
    if (!newSkillInput.trim()) return;
    const skill = newSkillInput.trim();
    if (!formData.technicalSkills.includes(skill)) {
      setFormData((prev) => ({
        ...prev,
        technicalSkills: [...prev.technicalSkills, skill],
      }));
    }
    setNewSkillInput('');
  };

  const handleRemoveSkill = (skillToRemove) => {
    setFormData((prev) => ({
      ...prev,
      technicalSkills: prev.technicalSkills.filter((s) => s !== skillToRemove),
    }));
  };

  const handleSaveAndAnalyze = async () => {
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      await resumeService.updateExtractedData(resumeId, {
        ...formData,
        graduationYear: formData.graduationYear ? parseInt(formData.graduationYear) : null,
      });

      setSuccess('Details saved! Generating automated resume quality analysis...');
      await resumeService.analyzeResume(resumeId);

      setTimeout(() => {
        navigate(`/student/analyzer?id=${resumeId}`);
      }, 1000);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to update resume details. Please try again.');
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Review Extracted Resume Data">
        <LoadingSpinner text="Retrieving parsed fields..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Review & Verify Extracted Details"
      subtitle="Verify the automated PDF/DOCX extraction. You can adjust fields or add missing skills before computing your quality score."
      action={
        <button
          type="button"
          disabled={saving}
          onClick={handleSaveAndAnalyze}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-sm disabled:opacity-60 transition-all cursor-pointer"
        >
          {saving ? 'Analyzing Quality...' : 'Save & Analyze Quality'}
          <ArrowRight className="w-4 h-4" />
        </button>
      }
    >
      <div className="max-w-4xl mx-auto space-y-6">
        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-sm flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600" />
            <span>{success}</span>
          </div>
        )}

        {/* Personal Details Section */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
          <h3 className="font-bold text-base text-slate-900 dark:text-white mb-4 flex items-center gap-2">
            <User className="w-4 h-4 text-blue-600" />
            Personal &amp; Contact Details
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                value={formData.candidateName}
                onChange={(e) => setFormData({ ...formData, candidateName: e.target.value })}
                placeholder="Candidate Full Name"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Email Address
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="candidate@example.com"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Phone Number
              </label>
              <input
                type="text"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                placeholder="+91 9876543210"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Location
              </label>
              <input
                type="text"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                placeholder="City, Country"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>

        {/* Education Credentials */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
          <h3 className="font-bold text-base text-slate-900 dark:text-white mb-4 flex items-center gap-2">
            <GraduationCap className="w-4 h-4 text-indigo-600" />
            Education Credentials
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Degree / Qualification
              </label>
              <input
                type="text"
                value={formData.degree}
                onChange={(e) => setFormData({ ...formData, degree: e.target.value })}
                placeholder="e.g. B.Tech Computer Science"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Institution / University
              </label>
              <input
                type="text"
                value={formData.institution}
                onChange={(e) => setFormData({ ...formData, institution: e.target.value })}
                placeholder="University or College Name"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Graduation Year
              </label>
              <input
                type="number"
                value={formData.graduationYear}
                onChange={(e) => setFormData({ ...formData, graduationYear: e.target.value })}
                placeholder="2025"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                CGPA / GPA
              </label>
              <input
                type="text"
                value={formData.cgpa}
                onChange={(e) => setFormData({ ...formData, cgpa: e.target.value })}
                placeholder="8.5 / 10"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>

        {/* Technical Skills Section */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
            <div>
              <h3 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-purple-600" />
                Detected Technical Skills ({formData.technicalSkills.length})
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">Click the &times; on any badge to remove, or add new skills below</p>
            </div>

            {/* Add skill input */}
            <form onSubmit={handleAddSkill} className="flex gap-2">
              <input
                type="text"
                value={newSkillInput}
                onChange={(e) => setNewSkillInput(e.target.value)}
                placeholder="Add skill (e.g. Docker)"
                className="px-3 py-1.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                type="submit"
                className="px-3 py-1.5 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-300 text-xs font-semibold flex items-center gap-1 cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
                Add
              </button>
            </form>
          </div>

          <div className="flex flex-wrap gap-2 min-h-[60px] p-4 rounded-2xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800">
            {formData.technicalSkills.length > 0 ? (
              formData.technicalSkills.map((skill) => (
                <SkillBadge
                  key={skill}
                  name={skill}
                  variant="neutral"
                  onRemove={() => handleRemoveSkill(skill)}
                />
              ))
            ) : (
              <span className="text-xs text-slate-400">No skills entered. Add your core programming languages and tools.</span>
            )}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ResumeReviewPage;
