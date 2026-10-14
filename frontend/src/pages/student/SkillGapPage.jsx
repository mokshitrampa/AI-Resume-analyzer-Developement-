import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import SkillBadge from '../../components/SkillBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import { resumeService } from '../../services/resumeService';
import { jobService } from '../../services/jobService';
import { matchingService } from '../../services/matchingService';
import {
  TrendingUp,
  Target,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Lightbulb,
  Clock,
  BookOpen,
  ArrowRight,
  Code2
} from 'lucide-react';

const SkillGapPage = () => {
  const [searchParams] = useSearchParams();
  const initialResumeId = searchParams.get('resumeId');
  const initialJobId = searchParams.get('jobId');

  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState(initialResumeId || '');
  const [selectedJobId, setSelectedJobId] = useState(initialJobId || '');

  const [gapData, setGapData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadSelectors();
  }, []);

  useEffect(() => {
    if (selectedResumeId && selectedJobId) {
      loadSkillGap();
    }
  }, [selectedResumeId, selectedJobId]);

  const loadSelectors = async () => {
    try {
      setLoading(true);
      const [resumesList, jobsList] = await Promise.all([
        resumeService.getAllResumes(),
        jobService.getAllJobs(),
      ]);
      setResumes(resumesList);
      setJobs(jobsList);

      const rId = initialResumeId || (resumesList[0] ? resumesList[0].id : '');
      const jId = initialJobId || (jobsList[0] ? jobsList[0].id : '');
      setSelectedResumeId(rId);
      setSelectedJobId(jId);
    } catch (err) {
      console.error(err);
      setError('Failed to load profile and vacancy lists.');
    } finally {
      setLoading(false);
    }
  };

  const loadSkillGap = async () => {
    if (!selectedResumeId || !selectedJobId) return;

    try {
      setAnalyzing(true);
      setError('');
      const data = await matchingService.getSkillGapAnalysis(selectedResumeId, selectedJobId);
      setGapData(data);
    } catch (err) {
      console.error(err);
      setError('Could not calculate skill gap. Please try another selection.');
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout title="Skill Gap & Learning Roadmap">
        <LoadingSpinner text="Fetching competencies and career trajectories..." size="lg" />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Skill Gap & Learning Roadmap"
      subtitle="Discover missing technologies required for target engineering positions and follow a structured 5-phase roadmap."
    >
      <div className="space-y-8">
        {/* Selector Filters */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-2">
              Select Resume
            </label>
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
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-2">
              Select Target Job Opening
            </label>
            <select
              value={selectedJobId}
              onChange={(e) => setSelectedJobId(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
            >
              {jobs.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.title} • {j.companyName}
                </option>
              ))}
            </select>
          </div>
        </div>

        {analyzing ? (
          <LoadingSpinner text="Analyzing missing skill tier requirements..." size="lg" />
        ) : gapData ? (
          <div className="space-y-8 animate-in fade-in duration-200">
            {/* Target Role Overview */}
            <div className="bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 rounded-3xl p-6 sm:p-8 text-white shadow-lg">
              <span className="text-xs font-bold uppercase tracking-wider text-blue-200">Target Opportunity</span>
              <h3 className="text-2xl sm:text-3xl font-extrabold mt-1">{gapData.targetRole}</h3>
              <p className="text-sm text-blue-100 mt-1">{gapData.companyName}</p>

              <div className="mt-6 flex flex-wrap gap-4 pt-4 border-t border-white/20 text-xs">
                <div>
                  <span className="text-blue-200 block">Existing Skills:</span>
                  <span className="font-bold text-base">{gapData.existingSkills?.length || 0}</span>
                </div>
                <div className="border-l border-white/20 pl-4">
                  <span className="text-blue-200 block">Matching Requirements:</span>
                  <span className="font-bold text-base text-emerald-300">{gapData.matchingSkills?.length || 0}</span>
                </div>
                <div className="border-l border-white/20 pl-4">
                  <span className="text-blue-200 block">Identified Skill Gaps:</span>
                  <span className="font-bold text-base text-amber-300">{gapData.missingSkills?.length || 0}</span>
                </div>
              </div>
            </div>

            {/* Overlap Badges Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Matching */}
              <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
                <h4 className="font-bold text-sm text-emerald-700 dark:text-emerald-400 flex items-center gap-2 mb-3">
                  <CheckCircle2 className="w-4 h-4" />
                  Verified Skills You Possess ({gapData.matchingSkills?.length || 0})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {gapData.matchingSkills?.length > 0 ? (
                    gapData.matchingSkills.map((s) => <SkillBadge key={s} name={s} variant="matched" />)
                  ) : (
                    <span className="text-xs text-slate-400">No overlapping skills detected for this vacancy.</span>
                  )}
                </div>
              </div>

              {/* Missing Skills Sorted by Priority */}
              <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
                <h4 className="font-bold text-sm text-rose-700 dark:text-rose-400 flex items-center gap-2 mb-3">
                  <AlertTriangle className="w-4 h-4" />
                  Priority Skills to Acquire ({gapData.prioritySkills?.length || 0})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {gapData.prioritySkills?.length > 0 ? (
                    gapData.prioritySkills.map((s) => <SkillBadge key={s} name={s} variant="missing" />)
                  ) : (
                    <span className="text-xs text-emerald-600 font-medium">
                      Zero skill gaps detected! You meet all listed technical prerequisites for this role.
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Personalized 5-Phase Learning Roadmap */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm">
              <div className="flex items-center gap-2 mb-6">
                <TrendingUp className="w-6 h-6 text-indigo-600" />
                <div>
                  <h3 className="font-bold text-lg text-slate-900 dark:text-white">
                    Personalized 5-Phase Learning Roadmap
                  </h3>
                  <p className="text-xs text-slate-500">
                    Sequenced based on foundational architectural hierarchy (Languages &amp; Frameworks → Databases → Cloud &amp; Tooling)
                  </p>
                </div>
              </div>

              <div className="space-y-6">
                {gapData.learningRoadmap?.map((step) => (
                  <div
                    key={step.phase}
                    className="relative pl-8 pb-6 border-l-2 border-indigo-200 dark:border-indigo-900/60 last:border-0 last:pb-0"
                  >
                    {/* Circle badge */}
                    <div className="absolute -left-3.5 top-0 w-7 h-7 rounded-full bg-indigo-600 text-white flex items-center justify-center font-bold text-xs shadow-md">
                      {step.phase}
                    </div>

                    <div className="bg-slate-50 dark:bg-slate-950/60 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 space-y-3">
                      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                        <h4 className="font-bold text-sm text-slate-900 dark:text-white">{step.title}</h4>
                        <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-slate-500 bg-white dark:bg-slate-900 px-2.5 py-1 rounded-full border border-slate-200 dark:border-slate-800">
                          <Clock className="w-3 h-3 text-blue-500" />
                          {step.estimatedDuration}
                        </span>
                      </div>

                      <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                        {step.description}
                      </p>

                      <div className="flex flex-wrap items-center gap-1.5 pt-1">
                        <span className="text-[11px] font-semibold text-slate-500">Focus Skills:</span>
                        {step.skills?.map((s) => (
                          <SkillBadge key={s} name={s} variant="preferred" size="sm" />
                        ))}
                      </div>

                      <div className="pt-2 border-t border-slate-200/60 dark:border-slate-800 text-[11px] text-slate-500 flex items-center gap-1.5">
                        <BookOpen className="w-3.5 h-3.5 text-indigo-500" />
                        <span>Recommended Resource: {step.resourceSuggestion}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Suggested Portfolio Projects */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm">
              <div className="flex items-center gap-2 mb-4">
                <Lightbulb className="w-6 h-6 text-amber-500" />
                <h3 className="font-bold text-lg text-slate-900 dark:text-white">
                  Targeted Portfolio Projects to Bridge These Gaps
                </h3>
              </div>
              <p className="text-xs text-slate-500 mb-6">
                Completing these projects demonstrates verified mastery of the missing technologies on your resume.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {gapData.suggestedProjects?.map((proj, idx) => (
                  <div
                    key={idx}
                    className="p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-950/40 flex flex-col justify-between"
                  >
                    <div>
                      <div className="w-8 h-8 rounded-lg bg-amber-50 dark:bg-amber-950/40 text-amber-600 flex items-center justify-center font-bold text-xs mb-3">
                        P{idx + 1}
                      </div>
                      <p className="text-xs font-semibold text-slate-800 dark:text-slate-200 leading-relaxed">
                        {proj}
                      </p>
                    </div>
                    <div className="mt-4 pt-3 border-t border-slate-200/60 dark:border-slate-800 text-[11px] text-blue-600 dark:text-blue-400 font-medium flex items-center gap-1">
                      <Code2 className="w-3 h-3" />
                      Add to GitHub Portfolio
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </DashboardLayout>
  );
};

export default SkillGapPage;
