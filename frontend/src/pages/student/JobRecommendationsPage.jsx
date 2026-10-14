import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import Modal from '../../components/Modal';
import LoadingSpinner from '../../components/LoadingSpinner';
import { jobService } from '../../services/jobService';
import { resumeService } from '../../services/resumeService';
import {
  Compass,
  Search,
  MapPin,
  Briefcase,
  Bookmark,
  BookmarkCheck,
  Send,
  ArrowRight,
  Filter,
  DollarSign,
  ExternalLink,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

const JobRecommendationsPage = () => {
  const [jobs, setJobs] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTitle, setSearchTitle] = useState('');
  const [searchLocation, setSearchLocation] = useState('');
  const [workModeFilter, setWorkModeFilter] = useState('ALL');

  // Application modal state
  const [applyModalOpen, setApplyModalOpen] = useState(false);
  const [selectedJob, setSelectedJob] = useState(null);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [coverLetter, setCoverLetter] = useState('');
  const [applying, setApplying] = useState(false);
  const [applySuccess, setApplySuccess] = useState('');
  const [applyError, setApplyError] = useState('');

  useEffect(() => {
    loadJobs();
  }, []);

  const loadJobs = async () => {
    try {
      setLoading(true);
      const [jobsList, resumesList] = await Promise.all([
        jobService.getAllJobs({ title: searchTitle, location: searchLocation }),
        resumeService.getAllResumes(),
      ]);

      setResumes(resumesList);
      if (resumesList.length > 0) {
        setSelectedResumeId(resumesList[0].id);
        // If resume exists, get recommended ranked jobs
        try {
          const recs = await jobService.getRecommendedJobs(resumesList[0].id);
          setJobs(recs);
        } catch {
          setJobs(jobsList);
        }
      } else {
        setJobs(jobsList);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const list = await jobService.getAllJobs({ title: searchTitle, location: searchLocation });
      setJobs(list);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSave = async (job) => {
    try {
      if (job.saved) {
        await jobService.unsaveJob(job.id);
      } else {
        await jobService.saveJob(job.id);
      }
      setJobs((prev) =>
        prev.map((j) => (j.id === job.id ? { ...j, saved: !j.saved } : j))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const openApplyModal = (job) => {
    setSelectedJob(job);
    setApplySuccess('');
    setApplyError('');
    setApplyModalOpen(true);
  };

  const handleApply = async () => {
    if (!selectedJob) return;
    setApplying(true);
    setApplyError('');
    setApplySuccess('');

    try {
      await jobService.applyForJob(selectedJob.id, selectedResumeId, coverLetter);
      setApplySuccess('Application successfully submitted to ' + selectedJob.companyName + '!');
      setTimeout(() => {
        setApplyModalOpen(false);
      }, 1500);
    } catch (err) {
      setApplyError(err.response?.data?.message || 'Failed to submit application.');
    } finally {
      setApplying(false);
    }
  };

  const filteredJobs = jobs.filter((j) => {
    if (workModeFilter !== 'ALL' && j.workMode !== workModeFilter) return false;
    return true;
  });

  return (
    <DashboardLayout
      title="Job Opportunities & Recommendations"
      subtitle="Discover engineering vacancies matching your candidate skills, ranked with computed match scores."
    >
      <div className="space-y-6">
        {/* Search & Filter Bar */}
        <form onSubmit={handleSearch} className="bg-white dark:bg-slate-900 rounded-3xl p-5 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
            <input
              type="text"
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              placeholder="Search by role or keyword (e.g. Java, React, Cloud)"
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="relative flex-1">
            <MapPin className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
            <input
              type="text"
              value={searchLocation}
              onChange={(e) => setSearchLocation(e.target.value)}
              placeholder="Location or 'Remote'"
              className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <select
            value={workModeFilter}
            onChange={(e) => setWorkModeFilter(e.target.value)}
            className="px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">All Work Modes</option>
            <option value="REMOTE">Remote Only</option>
            <option value="HYBRID">Hybrid</option>
            <option value="ONSITE">On-Site</option>
          </select>

          <button
            type="submit"
            className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-sm transition-colors cursor-pointer"
          >
            Search
          </button>
        </form>

        {loading ? (
          <LoadingSpinner text="Fetching recommended job vacancies..." size="lg" />
        ) : filteredJobs.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredJobs.map((job) => (
              <div
                key={job.id}
                className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow group"
              >
                <div>
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                      {job.workMode}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleToggleSave(job)}
                      className="text-slate-400 hover:text-blue-600 transition-colors"
                      title={job.saved ? 'Remove bookmark' : 'Bookmark job'}
                    >
                      {job.saved ? (
                        <BookmarkCheck className="w-5 h-5 text-blue-600" />
                      ) : (
                        <Bookmark className="w-5 h-5" />
                      )}
                    </button>
                  </div>

                  <h3 className="font-bold text-base text-slate-900 dark:text-white group-hover:text-blue-600 transition-colors">
                    {job.title}
                  </h3>
                  <p className="text-xs text-slate-500 font-medium mb-3">
                    {job.companyName} • {job.location}
                  </p>

                  {job.matchScore !== null && (
                    <div className="mb-4 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 text-xs font-bold border border-blue-200 dark:border-blue-900/40">
                      <Compass className="w-3.5 h-3.5" />
                      <span>{job.matchScore}% Compatibility</span>
                    </div>
                  )}

                  <p className="text-xs text-slate-600 dark:text-slate-400 line-clamp-3 mb-4 leading-relaxed">
                    {job.description}
                  </p>

                  <div className="space-y-1 mb-4">
                    <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Required Skills:</span>
                    <div className="flex flex-wrap gap-1">
                      {job.requiredSkills?.map((s) => (
                        <span key={s} className="px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-[11px] text-slate-700 dark:text-slate-300">
                          {s}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-slate-100 dark:border-slate-800 space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-500">
                    <span className="font-bold text-slate-800 dark:text-slate-200">{job.salaryRange || 'Competitive'}</span>
                    <span>{job.experienceLevel || 'Entry Level'}</span>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => openApplyModal(job)}
                      className="flex-1 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition-colors flex items-center justify-center gap-1.5 cursor-pointer shadow-sm"
                    >
                      <Send className="w-3.5 h-3.5" />
                      Apply Now
                    </button>
                    <Link
                      to={`/student/matcher?jobId=${job.id}`}
                      className="py-2 px-3 rounded-xl border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 text-xs font-medium transition-colors"
                      title="Inspect match score breakdown"
                    >
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800 shadow-sm">
            <Briefcase className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
            <h4 className="font-bold text-base text-slate-900 dark:text-white">No Matching Jobs Found</h4>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
              Try adjusting your search criteria or clear filters to view all available positions.
            </p>
          </div>
        )}

        {/* Internal Application Modal */}
        <Modal
          isOpen={applyModalOpen}
          onClose={() => setApplyModalOpen(false)}
          title={`Apply for ${selectedJob?.title || 'Position'}`}
        >
          {applySuccess ? (
            <div className="p-4 rounded-xl bg-emerald-50 text-emerald-800 text-sm flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
              <span>{applySuccess}</span>
            </div>
          ) : (
            <div className="space-y-4">
              {applyError && (
                <div className="p-3 rounded-xl bg-rose-50 text-rose-800 text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
                  <span>{applyError}</span>
                </div>
              )}

              <p className="text-xs text-slate-500">
                Company: <span className="font-semibold text-slate-800 dark:text-slate-200">{selectedJob?.companyName}</span> • Location: {selectedJob?.location}
              </p>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                  Attach Resume Version
                </label>
                <select
                  value={selectedResumeId}
                  onChange={(e) => setSelectedResumeId(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm"
                >
                  {resumes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.originalFilename} (v{r.version})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                  Cover Letter / Note to Recruiter (Optional)
                </label>
                <textarea
                  rows={4}
                  value={coverLetter}
                  onChange={(e) => setCoverLetter(e.target.value)}
                  placeholder="Introduce yourself and explain why you are excited for this role..."
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="pt-2 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setApplyModalOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-300 text-sm font-medium"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={applying}
                  onClick={handleApply}
                  className="px-6 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold disabled:opacity-60"
                >
                  {applying ? 'Submitting Application...' : 'Submit Application'}
                </button>
              </div>
            </div>
          )}
        </Modal>
      </div>
    </DashboardLayout>
  );
};

export default JobRecommendationsPage;
