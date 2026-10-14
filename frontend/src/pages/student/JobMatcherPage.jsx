import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import ScoreRing from '../../components/ScoreRing';
import SkillBadge from '../../components/SkillBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import { resumeService } from '../../services/resumeService';
import { jobService } from '../../services/jobService';
import { matchingService } from '../../services/matchingService';
import {
  Crosshair,
  Briefcase,
  Sparkles,
  CheckCircle2,
  XCircle,
  TrendingUp,
  ArrowRight,
  FileText,
  Sliders,
  Cpu,
  BookmarkCheck,
  Building
} from 'lucide-react';

const JobMatcherPage = () => {
  const [searchParams] = useSearchParams();
  const preselectedJobId = searchParams.get('jobId');

  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [selectedJobId, setSelectedJobId] = useState(preselectedJobId || '');
  const [mode, setMode] = useState('catalog'); // 'catalog' or 'custom'

  // Custom Job input state
  const [customTitle, setCustomTitle] = useState('');
  const [customDescription, setCustomDescription] = useState('');
  const [customReqSkills, setCustomReqSkills] = useState('');
  const [customPrefSkills, setCustomPrefSkills] = useState('');

  const [loading, setLoading] = useState(true);
  const [matching, setMatching] = useState(false);
  const [matchResult, setMatchResult] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [resumesList, jobsList] = await Promise.all([
        resumeService.getAllResumes(),
        jobService.getAllJobs(),
      ]);
      setResumes(resumesList);
      setJobs(jobsList);

      if (resumesList.length > 0) {
        setSelectedResumeId(resumesList[0].id);
      }
      if (jobsList.length > 0 && !selectedJobId) {
        setSelectedJobId(jobsList[0].id);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to load matching candidates and job openings.');
    } finally {
      setLoading(false);
    }
  };

  const handleMatch = async () => {
    if (!selectedResumeId) {
      setError('Please select a resume to match.');
      return;
    }

    setMatching(true);
    setError('');
    setMatchResult(null);

    try {
      let result;
      if (mode === 'catalog') {
        if (!selectedJobId) {
          setError('Please select a target job position.');
          setMatching(false);
          return;
        }
        result = await matchingService.matchResumeWithJob(selectedResumeId, selectedJobId);
      } else {
        if (!customTitle || !customDescription) {
          setError('Please provide a job title and job description.');
          setMatching(false);
          return;
        }
        result = await matchingService.matchResumeWithCustomJobText(
          selectedResumeId,
          customTitle,
          customDescription,
          customReqSkills,
          customPrefSkills
        );
      }
      setMatchResult(result);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Matching failed. Please verify inputs.');
    } finally {
      setMatching(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Job Matching Engine">
        <LoadingSpinner text="Loading career positions and resume profiles..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Job Matching Engine"
      subtitle="Pure Java TF-IDF vectorization and weighted cosine similarity benchmarking your resume against job requirements."
    >
      <div className="space-y-8">
        {/* Controls Card */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-end">
            {/* Resume Selection */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-2">
                1. Select Active Resume Version
              </label>
              {resumes.length > 0 ? (
                <select
                  value={selectedResumeId}
                  onChange={(e) => setSelectedResumeId(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                >
                  {resumes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.originalFilename} (v{r.version})
                    </option>
                  ))}
                </select>
              ) : (
                <Link
                  to="/student/upload"
                  className="inline-flex items-center gap-2 text-sm text-blue-600 font-semibold hover:underline"
                >
                  No resume found. Upload one first →
                </Link>
              )}
            </div>

            {/* Mode Toggle */}
            <div className="flex justify-end">
              <div className="inline-flex p-1 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 w-full sm:w-auto">
                <button
                  type="button"
                  onClick={() => setMode('catalog')}
                  className={`flex-1 sm:flex-none px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                    mode === 'catalog'
                      ? 'bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-sm'
                      : 'text-slate-600 dark:text-slate-400'
                  }`}
                >
                  Select from Database Jobs
                </button>
                <button
                  type="button"
                  onClick={() => setMode('custom')}
                  className={`flex-1 sm:flex-none px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                    mode === 'custom'
                      ? 'bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-sm'
                      : 'text-slate-600 dark:text-slate-400'
                  }`}
                >
                  Paste Custom Job Description
                </button>
              </div>
            </div>
          </div>

          {/* Mode Form Content */}
          {mode === 'catalog' ? (
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-2">
                2. Choose Target Position
              </label>
              <select
                value={selectedJobId}
                onChange={(e) => setSelectedJobId(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
              >
                {jobs.map((j) => (
                  <option key={j.id} value={j.id}>
                    {j.title} • {j.companyName} ({j.location})
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div className="space-y-4 pt-2 border-t border-slate-100 dark:border-slate-800">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                    Target Job Title
                  </label>
                  <input
                    type="text"
                    required
                    value={customTitle}
                    onChange={(e) => setCustomTitle(e.target.value)}
                    placeholder="e.g. Senior Java Cloud Engineer"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                    Required Skills (Comma-separated)
                  </label>
                  <input
                    type="text"
                    value={customReqSkills}
                    onChange={(e) => setCustomReqSkills(e.target.value)}
                    placeholder="Java, Spring Boot, MySQL, REST API"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                  Job Description Text
                </label>
                <textarea
                  rows={4}
                  required
                  value={customDescription}
                  onChange={(e) => setCustomDescription(e.target.value)}
                  placeholder="Paste the complete job description, duties, and qualifications here..."
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          )}

          {error && (
            <p className="text-xs text-rose-600 font-semibold">{error}</p>
          )}

          <button
            type="button"
            disabled={matching}
            onClick={handleMatch}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-semibold text-sm shadow-md shadow-indigo-500/20 disabled:opacity-60 transition-all cursor-pointer"
          >
            {matching ? 'Calculating Cosine Similarity...' : 'Run Job Match Engine'}
            <Crosshair className="w-4 h-4" />
          </button>
        </div>

        {/* Match Result Display */}
        {matchResult && (
          <div className="space-y-8 animate-in fade-in duration-300">
            {/* Top Cards: Score Ring & Formula Breakdown */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Score Ring Card */}
              <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                  Overall Compatibility
                </span>
                <ScoreRing
                  score={matchResult.overallScore}
                  size={160}
                  strokeWidth={12}
                  label="Match Score"
                  sublabel="Calculated"
                />
                <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 w-full space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Target Role:</span>
                    <span className="font-semibold text-slate-800 dark:text-slate-200 truncate max-w-[170px]">{matchResult.jobTitle}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Company:</span>
                    <span className="font-semibold text-slate-800 dark:text-slate-200">{matchResult.companyName}</span>
                  </div>
                </div>

                {matchResult.jobId && (
                  <Link
                    to={`/student/skill-gap?resumeId=${matchResult.resumeId}&jobId=${matchResult.jobId}`}
                    className="mt-6 w-full inline-flex items-center justify-center gap-2 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold transition-colors shadow-sm"
                  >
                    View Skill Gap Roadmap
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                )}
              </div>

              {/* Component Contribution Breakdown */}
              <div className="lg:col-span-2 bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
                <div className="flex items-center gap-2 mb-2">
                  <Cpu className="w-5 h-5 text-indigo-600" />
                  <h3 className="font-bold text-base text-slate-900 dark:text-white">
                    Mathematical Weight Breakdown
                  </h3>
                </div>
                <p className="text-xs text-slate-500 mb-6">
                  Weights automatically redistribute if criteria are not required by the job posting.
                </p>

                <div className="space-y-4">
                  {matchResult.components?.map((c, idx) => (
                    <div key={idx}>
                      <div className="flex justify-between text-xs font-semibold mb-1">
                        <span className="text-slate-700 dark:text-slate-300">
                          {c.name} ({c.effectiveWeightPercentage}%)
                        </span>
                        <span className="text-blue-600 dark:text-blue-400">
                          Score: {c.rawScore}% → +{c.contributedPoints} pts
                        </span>
                      </div>
                      <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                        <div
                          className="h-full bg-blue-600 rounded-full"
                          style={{ width: `${c.rawScore}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>

                <div className="mt-6 p-4 rounded-2xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800 text-xs font-mono text-slate-700 dark:text-slate-300 whitespace-pre-line">
                  {matchResult.explanation}
                </div>
              </div>
            </div>

            {/* Overlap & Gaps */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Matching Skills */}
              <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
                <h4 className="font-bold text-sm text-emerald-700 dark:text-emerald-400 flex items-center gap-2 mb-4">
                  <CheckCircle2 className="w-4 h-4" />
                  Matching Candidate Skills ({matchResult.matchingSkills?.length || 0})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {matchResult.matchingSkills?.map((s) => (
                    <SkillBadge key={s} name={s} variant="matched" />
                  ))}
                </div>
              </div>

              {/* Missing Skills */}
              <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
                <h4 className="font-bold text-sm text-rose-700 dark:text-rose-400 flex items-center gap-2 mb-4">
                  <XCircle className="w-4 h-4" />
                  Missing Job Skills ({matchResult.missingSkills?.length || 0})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {matchResult.missingSkills?.map((s) => (
                    <SkillBadge key={s} name={s} variant="missing" />
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default JobMatcherPage;
