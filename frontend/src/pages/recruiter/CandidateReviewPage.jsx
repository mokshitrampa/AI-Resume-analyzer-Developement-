import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import ScoreRing from '../../components/ScoreRing';
import Modal from '../../components/Modal';
import { recruiterService } from '../../services/recruiterService';
import {
  Users,
  Briefcase,
  CheckCircle2,
  XCircle,
  Eye,
  Clock,
  Filter,
  FileText,
  Mail,
  Phone,
  Calendar,
  AlertCircle,
  ArrowUpDown,
  MessageSquare
} from 'lucide-react';

const CandidateReviewPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialJobId = searchParams.get('jobId');

  const [jobs, setJobs] = useState([]);
  const [selectedJobId, setSelectedJobId] = useState(initialJobId || '');
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingApps, setLoadingApps] = useState(false);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Selected Candidate Modal
  const [selectedCandidate, setSelectedCandidate] = useState(null);

  // Status Update Modal
  const [statusModalCandidate, setStatusModalCandidate] = useState(null);
  const [newStatus, setNewStatus] = useState('SHORTLISTED');
  const [statusNotes, setStatusNotes] = useState('');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    loadJobs();
  }, []);

  useEffect(() => {
    if (selectedJobId) {
      loadApplications(selectedJobId);
      setSearchParams({ jobId: selectedJobId });
    } else {
      setApplications([]);
    }
  }, [selectedJobId]);

  const loadJobs = async () => {
    try {
      setLoading(true);
      const jobsList = await recruiterService.getMyJobs();
      setJobs(jobsList);
      if (!selectedJobId && jobsList.length > 0) {
        setSelectedJobId(jobsList[0].id.toString());
      }
    } catch (err) {
      console.error(err);
      setError('Failed to load published job vacancies.');
    } finally {
      setLoading(false);
    }
  };

  const loadApplications = async (jobId) => {
    try {
      setLoadingApps(true);
      setError('');
      const data = await recruiterService.getJobApplications(jobId);
      setApplications(data);
    } catch (err) {
      console.error(err);
      setError('Failed to load candidate applications for this position.');
    } finally {
      setLoadingApps(false);
    }
  };

  const handleUpdateStatus = async (e) => {
    e.preventDefault();
    if (!statusModalCandidate) return;
    setUpdating(true);
    try {
      await recruiterService.updateApplicationStatus(
        statusModalCandidate.id,
        newStatus,
        statusNotes
      );
      setMessage(`Application status updated to ${newStatus}.`);
      setStatusModalCandidate(null);
      setStatusNotes('');
      loadApplications(selectedJobId);
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to update candidate status.');
    } finally {
      setUpdating(false);
    }
  };

  const filteredApplications = applications
    .filter((app) => (statusFilter === 'ALL' ? true : app.status === statusFilter))
    .sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0));

  const currentJob = jobs.find((j) => j.id.toString() === selectedJobId?.toString());

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SHORTLISTED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-800">
            Shortlisted
          </span>
        );
      case 'REJECTED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-100 dark:bg-rose-950/60 text-rose-700 dark:text-rose-400 border border-rose-300 dark:border-rose-800">
            Rejected
          </span>
        );
      case 'REVIEWED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-400 border border-blue-300 dark:border-blue-800">
            Reviewed
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-400 border border-amber-300 dark:border-amber-800">
            Pending
          </span>
        );
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Candidate Applications Review">
        <LoadingSpinner text="Loading your active recruitment openings..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Candidate Applications Review"
      subtitle="Examine algorithm-ranked candidate submissions, evaluate skill overlap scores, and make shortlisting decisions."
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

        {/* Position Selector Bar */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex-1 max-w-md">
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">
              Select Position to Review
            </label>
            <select
              value={selectedJobId}
              onChange={(e) => setSelectedJobId(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {jobs.map((job) => (
                <option key={job.id} value={job.id}>
                  {job.title} — {job.companyName} ({job.location})
                </option>
              ))}
            </select>
          </div>

          {currentJob && (
            <div className="flex items-center gap-4 text-xs text-slate-600 dark:text-slate-400">
              <div className="text-right">
                <span className="font-semibold block text-slate-900 dark:text-white">
                  {applications.length} Total Applicants
                </span>
                <span>
                  {applications.filter((a) => a.status === 'SHORTLISTED').length} Shortlisted
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Status Filters */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          {['ALL', 'PENDING', 'REVIEWED', 'SHORTLISTED', 'REJECTED'].map((filter) => (
            <button
              key={filter}
              type="button"
              onClick={() => setStatusFilter(filter)}
              className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition-colors cursor-pointer ${
                statusFilter === filter
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50'
              }`}
            >
              {filter === 'ALL' ? 'All Applicants' : filter}
              <span className="ml-1.5 opacity-70">
                (
                {filter === 'ALL'
                  ? applications.length
                  : applications.filter((a) => a.status === filter).length}
                )
              </span>
            </button>
          ))}
        </div>

        {/* Candidate List Table */}
        {loadingApps ? (
          <LoadingSpinner text="Ranking candidates by AI-calculated match score..." size="md" />
        ) : filteredApplications.length > 0 ? (
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 dark:bg-slate-950/60 border-b border-slate-200 dark:border-slate-800 text-xs font-bold uppercase text-slate-500 tracking-wider">
                  <tr>
                    <th className="px-6 py-4">Candidate</th>
                    <th className="px-6 py-4">Match Score</th>
                    <th className="px-6 py-4">Matching Skills</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4">Applied Date</th>
                    <th className="px-6 py-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {filteredApplications.map((app) => (
                    <tr
                      key={app.id}
                      className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <div className="font-bold text-slate-900 dark:text-white">
                          {app.studentName || 'Anonymous Student'}
                        </div>
                        <div className="text-xs text-slate-500 flex items-center gap-2 mt-0.5">
                          <span className="flex items-center gap-1">
                            <Mail className="w-3 h-3 text-slate-400" />
                            {app.studentEmail}
                          </span>
                        </div>
                      </td>

                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <span
                            className={`px-2.5 py-1 rounded-xl text-xs font-extrabold ${
                              app.matchScore >= 75
                                ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-400'
                                : app.matchScore >= 50
                                ? 'bg-blue-100 dark:bg-blue-950 text-blue-700 dark:text-blue-400'
                                : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300'
                            }`}
                          >
                            {app.matchScore != null ? `${Math.round(app.matchScore)}%` : 'N/A'}
                          </span>
                        </div>
                      </td>

                      <td className="px-6 py-4 max-w-xs">
                        <div className="flex flex-wrap gap-1">
                          {app.matchingSkills?.slice(0, 3).map((s) => (
                            <span
                              key={s}
                              className="px-2 py-0.5 rounded bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 text-[10px] font-medium"
                            >
                              ✓ {s}
                            </span>
                          ))}
                          {app.matchingSkills?.length > 3 && (
                            <span className="text-[10px] text-slate-400 font-semibold self-center">
                              +{app.matchingSkills.length - 3} more
                            </span>
                          )}
                        </div>
                      </td>

                      <td className="px-6 py-4">{getStatusBadge(app.status)}</td>

                      <td className="px-6 py-4 text-xs text-slate-500 whitespace-nowrap">
                        {app.appliedAt ? new Date(app.appliedAt).toLocaleDateString() : 'Recent'}
                      </td>

                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => setSelectedCandidate(app)}
                            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                            title="Inspect Candidate Profile"
                          >
                            <Eye className="w-4 h-4" />
                          </button>

                          <button
                            type="button"
                            onClick={() => {
                              setStatusModalCandidate(app);
                              setNewStatus(app.status === 'PENDING' ? 'SHORTLISTED' : app.status);
                              setStatusNotes(app.notes || '');
                            }}
                            className="px-3 py-1.5 rounded-xl bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-300 hover:bg-blue-100 text-xs font-semibold transition-colors cursor-pointer"
                          >
                            Update Status
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800">
            <Users className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
            <h4 className="font-bold text-slate-800 dark:text-slate-200 mb-1">
              No candidates found
            </h4>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              {statusFilter === 'ALL'
                ? 'No students have applied to this position yet. Students can match and apply directly from their dashboard.'
                : `No candidates currently under status "${statusFilter}".`}
            </p>
          </div>
        )}

        {/* Candidate Inspection Modal */}
        {selectedCandidate && (
          <Modal
            isOpen={!!selectedCandidate}
            onClose={() => setSelectedCandidate(null)}
            title={`Candidate Profile: ${selectedCandidate.studentName}`}
          >
            <div className="space-y-4 max-h-[75vh] overflow-y-auto pr-1 text-sm">
              <div className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 dark:bg-slate-800">
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white">
                    {selectedCandidate.studentName}
                  </h4>
                  <p className="text-xs text-slate-500">{selectedCandidate.studentEmail}</p>
                </div>
                <div className="text-right">
                  <div className="text-2xl font-black text-blue-600">
                    {Math.round(selectedCandidate.matchScore || 0)}%
                  </div>
                  <span className="text-[10px] uppercase font-bold text-slate-400">Match Score</span>
                </div>
              </div>

              <div>
                <h5 className="font-bold text-xs uppercase text-slate-500 tracking-wider mb-2">
                  Matching Required Skills
                </h5>
                <div className="flex flex-wrap gap-1.5">
                  {selectedCandidate.matchingSkills?.map((s) => (
                    <span
                      key={s}
                      className="px-2.5 py-1 rounded-lg bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 text-xs font-semibold"
                    >
                      ✓ {s}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <h5 className="font-bold text-xs uppercase text-slate-500 tracking-wider mb-2">
                  Missing Position Requirements
                </h5>
                <div className="flex flex-wrap gap-1.5">
                  {selectedCandidate.missingSkills?.length > 0 ? (
                    selectedCandidate.missingSkills.map((s) => (
                      <span
                        key={s}
                        className="px-2.5 py-1 rounded-lg bg-rose-100 dark:bg-rose-950 text-rose-700 dark:text-rose-300 text-xs font-semibold"
                      >
                        ✕ {s}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-emerald-600 font-semibold">
                      Candidate possesses all required skills!
                    </span>
                  )}
                </div>
              </div>

              {selectedCandidate.notes && (
                <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 text-xs text-amber-900 dark:text-amber-200">
                  <span className="font-bold block mb-1">Recruiter Notes:</span>
                  {selectedCandidate.notes}
                </div>
              )}

              <div className="flex justify-end pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedCandidate(null)}
                  className="px-4 py-2 rounded-xl bg-slate-200 dark:bg-slate-700 text-xs font-semibold"
                >
                  Close
                </button>
              </div>
            </div>
          </Modal>
        )}

        {/* Status Update Modal */}
        {statusModalCandidate && (
          <Modal
            isOpen={!!statusModalCandidate}
            onClose={() => setStatusModalCandidate(null)}
            title={`Update Status: ${statusModalCandidate.studentName}`}
          >
            <form onSubmit={handleUpdateStatus} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                  Application Decision
                </label>
                <select
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-semibold"
                >
                  <option value="SHORTLISTED">Shortlist for Interview</option>
                  <option value="REVIEWED">Mark as Under Review</option>
                  <option value="REJECTED">Decline Application</option>
                  <option value="PENDING">Keep Pending</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                  Internal Notes (Optional)
                </label>
                <textarea
                  rows={3}
                  value={statusNotes}
                  onChange={(e) => setStatusNotes(e.target.value)}
                  placeholder="e.g. Strong Java and Spring Boot experience. Schedule technical interview for next Tuesday."
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setStatusModalCandidate(null)}
                  className="px-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={updating}
                  className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold disabled:opacity-50"
                >
                  {updating ? 'Saving...' : 'Save Decision'}
                </button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </DashboardLayout>
  );
};

export default CandidateReviewPage;
