import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import Modal from '../../components/Modal';
import { recruiterService } from '../../services/recruiterService';
import {
  Briefcase,
  Users,
  PlusCircle,
  Edit,
  Trash2,
  MapPin,
  Calendar,
  Search,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  DollarSign
} from 'lucide-react';

const ManageJobsPage = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Edit Job State
  const [editingJob, setEditingJob] = useState(null);
  const [editFormData, setEditFormData] = useState({
    title: '',
    companyName: '',
    description: '',
    requirements: '',
    location: '',
    workMode: 'REMOTE',
    employmentType: 'FULL_TIME',
    minExperienceYears: 0,
    salaryRange: '',
    requiredSkills: '',
    preferredSkills: '',
  });
  const [savingEdit, setSavingEdit] = useState(false);

  // Delete Job State
  const [deletingJobId, setDeletingJobId] = useState(null);

  useEffect(() => {
    loadJobs();
  }, []);

  const loadJobs = async () => {
    try {
      setLoading(true);
      const data = await recruiterService.getMyJobs();
      setJobs(data);
    } catch (err) {
      console.error(err);
      setError('Failed to load posted jobs.');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenEdit = (job) => {
    setEditingJob(job);
    setEditFormData({
      title: job.title || '',
      companyName: job.companyName || '',
      description: job.description || '',
      requirements: job.requirements || '',
      location: job.location || '',
      workMode: job.workMode || 'REMOTE',
      employmentType: job.employmentType || 'FULL_TIME',
      minExperienceYears: job.minExperienceYears || 0,
      salaryRange: job.salaryRange || '',
      requiredSkills: Array.isArray(job.requiredSkills) ? job.requiredSkills.join(', ') : '',
      preferredSkills: Array.isArray(job.preferredSkills) ? job.preferredSkills.join(', ') : '',
    });
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    setSavingEdit(true);
    try {
      const payload = {
        ...editFormData,
        minExperienceYears: parseInt(editFormData.minExperienceYears) || 0,
        requiredSkills: editFormData.requiredSkills.split(',').map((s) => s.trim()).filter(Boolean),
        preferredSkills: editFormData.preferredSkills.split(',').map((s) => s.trim()).filter(Boolean),
      };
      await recruiterService.updateJob(editingJob.id, payload);
      setMessage('Job vacancy updated successfully!');
      setEditingJob(null);
      loadJobs();
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to update job details.');
    } finally {
      setSavingEdit(false);
    }
  };

  const handleDeleteJob = async (jobId) => {
    if (!window.confirm('Are you sure you want to delete this job posting? All candidate applications for this position will also be removed.')) {
      return;
    }
    try {
      await recruiterService.deleteJob(jobId);
      setMessage('Job listing deleted successfully.');
      setJobs(jobs.filter((j) => j.id !== jobId));
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to delete job.');
    }
  };

  const filteredJobs = jobs.filter((j) => {
    const q = search.toLowerCase();
    return (
      j.title?.toLowerCase().includes(q) ||
      j.companyName?.toLowerCase().includes(q) ||
      j.location?.toLowerCase().includes(q)
    );
  });

  return (
    <DashboardLayout
      title="Manage Job Listings"
      subtitle="View, edit, or archive your corporate openings and inspect candidate applicant flows."
      action={
        <Link
          to="/recruiter/post-job"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-sm transition-all"
        >
          <PlusCircle className="w-4 h-4" />
          Post New Position
        </Link>
      }
    >
      <div className="space-y-6">
        {message && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-sm flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600" />
            <span>{message}</span>
          </div>
        )}

        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-2">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-600" />
            <span>{error}</span>
          </div>
        )}

        {/* Filter and Search Bar */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-4 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Filter by title, company, or city..."
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <span className="text-xs text-slate-500 font-medium">
            Showing {filteredJobs.length} of {jobs.length} job postings
          </span>
        </div>

        {/* Jobs List */}
        {loading ? (
          <LoadingSpinner text="Loading published job postings..." size="md" />
        ) : filteredJobs.length > 0 ? (
          <div className="grid grid-cols-1 gap-4">
            {filteredJobs.map((job) => (
              <div
                key={job.id}
                className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col lg:flex-row lg:items-center justify-between gap-6 hover:border-slate-300 dark:hover:border-slate-700 transition-all"
              >
                <div className="space-y-2 max-w-2xl">
                  <div className="flex flex-wrap items-center gap-2.5">
                    <h3 className="font-bold text-base text-slate-900 dark:text-white">{job.title}</h3>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
                      Active
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-800">
                      {job.workMode}
                    </span>
                  </div>

                  <p className="text-xs text-slate-500 flex flex-wrap items-center gap-4">
                    <span>{job.companyName}</span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {job.location}
                    </span>
                    <span>•</span>
                    <span>Min {job.minExperienceYears} yrs experience</span>
                    {job.salaryRange && (
                      <>
                        <span>•</span>
                        <span className="flex items-center gap-1 text-slate-700 dark:text-slate-300 font-medium">
                          <DollarSign className="w-3.5 h-3.5 text-emerald-500" />
                          {job.salaryRange}
                        </span>
                      </>
                    )}
                  </p>

                  <div className="flex flex-wrap gap-1.5 pt-1">
                    <span className="text-[11px] font-semibold text-slate-400 mr-1 self-center">Required:</span>
                    {job.requiredSkills?.map((skill) => (
                      <span
                        key={skill}
                        className="px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-[11px] font-medium"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="flex items-center gap-2.5 flex-wrap lg:flex-nowrap">
                  <Link
                    to={`/recruiter/applications?jobId=${job.id}`}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition-colors shadow-sm cursor-pointer"
                  >
                    <Users className="w-3.5 h-3.5" />
                    Review Candidates
                  </Link>

                  <button
                    type="button"
                    onClick={() => handleOpenEdit(job)}
                    className="inline-flex items-center gap-1 px-3 py-2 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs font-semibold transition-colors cursor-pointer"
                  >
                    <Edit className="w-3.5 h-3.5 text-slate-500" />
                    Edit
                  </button>

                  <button
                    type="button"
                    onClick={() => handleDeleteJob(job.id)}
                    className="inline-flex items-center gap-1 px-3 py-2 rounded-xl bg-rose-50 dark:bg-rose-950/40 hover:bg-rose-100 dark:hover:bg-rose-900/40 text-rose-600 text-xs font-semibold transition-colors cursor-pointer"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800">
            <Briefcase className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
            <h4 className="font-bold text-slate-800 dark:text-slate-200 mb-1">No job postings found</h4>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mb-6">
              You haven't posted any jobs matching your criteria. Start publishing vacancies to receive candidate matches.
            </p>
            <Link
              to="/recruiter/post-job"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 text-white font-semibold text-xs shadow-sm hover:bg-blue-700"
            >
              <PlusCircle className="w-4 h-4" />
              Post Vacancy Now
            </Link>
          </div>
        )}

        {/* Edit Modal */}
        {editingJob && (
          <Modal
            isOpen={!!editingJob}
            onClose={() => setEditingJob(null)}
            title={`Edit Job: ${editingJob.title}`}
          >
            <form onSubmit={handleSaveEdit} className="space-y-4 max-h-[75vh] overflow-y-auto pr-1">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Job Title
                </label>
                <input
                  type="text"
                  required
                  value={editFormData.title}
                  onChange={(e) => setEditFormData({ ...editFormData, title: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                    Company Name
                  </label>
                  <input
                    type="text"
                    required
                    value={editFormData.companyName}
                    onChange={(e) => setEditFormData({ ...editFormData, companyName: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                    Location
                  </label>
                  <input
                    type="text"
                    required
                    value={editFormData.location}
                    onChange={(e) => setEditFormData({ ...editFormData, location: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                    Work Mode
                  </label>
                  <select
                    value={editFormData.workMode}
                    onChange={(e) => setEditFormData({ ...editFormData, workMode: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                  >
                    <option value="REMOTE">Remote</option>
                    <option value="HYBRID">Hybrid</option>
                    <option value="ON_SITE">On-site</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                    Type
                  </label>
                  <select
                    value={editFormData.employmentType}
                    onChange={(e) => setEditFormData({ ...editFormData, employmentType: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                  >
                    <option value="FULL_TIME">Full Time</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="CONTRACT">Contract</option>
                    <option value="PART_TIME">Part Time</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                    Min Exp (Yrs)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={editFormData.minExperienceYears}
                    onChange={(e) => setEditFormData({ ...editFormData, minExperienceYears: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Required Skills (comma separated)
                </label>
                <input
                  type="text"
                  required
                  value={editFormData.requiredSkills}
                  onChange={(e) => setEditFormData({ ...editFormData, requiredSkills: e.target.value })}
                  placeholder="Java, Spring Boot, MySQL, REST API"
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Preferred Skills (comma separated)
                </label>
                <input
                  type="text"
                  value={editFormData.preferredSkills}
                  onChange={(e) => setEditFormData({ ...editFormData, preferredSkills: e.target.value })}
                  placeholder="Docker, Kubernetes, AWS"
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Job Description
                </label>
                <textarea
                  rows={4}
                  required
                  value={editFormData.description}
                  onChange={(e) => setEditFormData({ ...editFormData, description: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingJob(null)}
                  className="px-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 text-xs font-semibold hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={savingEdit}
                  className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold disabled:opacity-50"
                >
                  {savingEdit ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </DashboardLayout>
  );
};

export default ManageJobsPage;
