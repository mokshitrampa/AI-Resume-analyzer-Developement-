import React, { useState, useEffect } from 'react';
import DashboardLayout from '../../layouts/DashboardLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import Modal from '../../components/Modal';
import { adminService } from '../../services/adminService';
import {
  BookOpen,
  Search,
  PlusCircle,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Tag,
  Filter
} from 'lucide-react';

const SkillDictionaryPage = () => {
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Add Skill Modal
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newSkillName, setNewSkillName] = useState('');
  const [newSkillCategory, setNewSkillCategory] = useState('PROGRAMMING_LANGUAGE');
  const [newSkillAliases, setNewSkillAliases] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadSkills();
  }, []);

  const loadSkills = async () => {
    try {
      setLoading(true);
      const data = await adminService.getSkills();
      setSkills(data || []);
    } catch (err) {
      console.error(err);
      setError('Failed to load skill taxonomy dictionary.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!newSkillName.trim()) return;

    setSubmitting(true);
    try {
      const aliasesList = newSkillAliases
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);

      const payload = {
        name: newSkillName.trim(),
        category: newSkillCategory,
        aliases: aliasesList,
      };

      await adminService.addSkill(payload);
      setMessage(`Skill "${newSkillName}" successfully added to NLP dictionary!`);
      setIsAddModalOpen(false);
      setNewSkillName('');
      setNewSkillAliases('');
      loadSkills();
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to add new skill.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteSkill = async (skill) => {
    if (!window.confirm(`Are you sure you want to remove skill "${skill.name}" from the taxonomy?`)) {
      return;
    }
    try {
      await adminService.deleteSkill(skill.id);
      setMessage(`Skill "${skill.name}" removed.`);
      setSkills(skills.filter((s) => s.id !== skill.id));
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error(err);
      setError('Failed to delete skill.');
    }
  };

  const categories = [
    'ALL',
    'PROGRAMMING_LANGUAGE',
    'FRAMEWORK',
    'DATABASE',
    'CLOUD',
    'TOOL',
    'METHODOLOGY',
    'OTHER',
  ];

  const filteredSkills = skills.filter((s) => {
    const matchesSearch =
      s.name?.toLowerCase().includes(search.toLowerCase()) ||
      s.aliases?.some((a) => a.toLowerCase().includes(search.toLowerCase()));
    const matchesCategory =
      categoryFilter === 'ALL' ? true : s.category === categoryFilter;
    return matchesSearch && matchesCategory;
  });

  const getCategoryColor = (category) => {
    switch (category) {
      case 'PROGRAMMING_LANGUAGE':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-950/60 dark:text-blue-300 border-blue-200 dark:border-blue-800';
      case 'FRAMEWORK':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-950/60 dark:text-purple-300 border-purple-200 dark:border-purple-800';
      case 'DATABASE':
        return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800';
      case 'CLOUD':
        return 'bg-sky-100 text-sky-800 dark:bg-sky-950/60 dark:text-sky-300 border-sky-200 dark:border-sky-800';
      case 'TOOL':
        return 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 border-amber-200 dark:border-amber-800';
      default:
        return 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border-slate-200 dark:border-slate-700';
    }
  };

  return (
    <DashboardLayout
      title="NLP Skill Dictionary &amp; Synonym Taxonomy"
      subtitle="Manage recognized technical competencies and configure normalization aliases (e.g., 'SpringBoot' &rarr; 'Spring Boot')."
      action={
        <button
          type="button"
          onClick={() => setIsAddModalOpen(true)}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold text-sm shadow-sm transition-all cursor-pointer"
        >
          <PlusCircle className="w-4 h-4" />
          Add Technical Skill
        </button>
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

        {/* Filter and Category Pills */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-4 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="relative w-full sm:w-80">
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search skill name or alias..."
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <span className="text-xs text-slate-500 font-medium">
              Showing {filteredSkills.length} of {skills.length} skills in dictionary
            </span>
          </div>

          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setCategoryFilter(cat)}
                className={`px-3 py-1 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors cursor-pointer ${
                  categoryFilter === cat
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
                }`}
              >
                {cat.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {/* Skills Grid */}
        {loading ? (
          <LoadingSpinner text="Loading NLP skill dictionary..." size="md" />
        ) : filteredSkills.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredSkills.map((skill) => (
              <div
                key={skill.id}
                className="bg-white dark:bg-slate-900 rounded-2xl p-4 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between gap-3 hover:border-slate-300 dark:hover:border-slate-700 transition-all"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <h4 className="font-bold text-sm text-slate-900 dark:text-white">
                      {skill.name}
                    </h4>
                    <span
                      className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getCategoryColor(
                        skill.category
                      )}`}
                    >
                      {skill.category?.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="mt-2.5">
                    <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block mb-1">
                      Aliases / Synonyms
                    </span>
                    <div className="flex flex-wrap gap-1">
                      {skill.aliases && skill.aliases.length > 0 ? (
                        skill.aliases.map((a) => (
                          <span
                            key={a}
                            className="px-2 py-0.5 rounded bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 text-[10px] font-medium border border-slate-200/60 dark:border-slate-700/60"
                          >
                            {a}
                          </span>
                        ))
                      ) : (
                        <span className="text-[10px] text-slate-400 italic">No aliases</span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex justify-end">
                  <button
                    type="button"
                    onClick={() => handleDeleteSkill(skill)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors cursor-pointer"
                    title="Remove Skill"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800">
            <BookOpen className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
            <h4 className="font-bold text-slate-800 dark:text-slate-200 mb-1">
              No skills found
            </h4>
            <p className="text-xs text-slate-500">Try modifying your category filter or search query.</p>
          </div>
        )}

        {/* Add Skill Modal */}
        {isAddModalOpen && (
          <Modal
            isOpen={isAddModalOpen}
            onClose={() => setIsAddModalOpen(false)}
            title="Add New Technical Skill"
          >
            <form onSubmit={handleAddSkill} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Canonical Skill Name
                </label>
                <input
                  type="text"
                  required
                  value={newSkillName}
                  onChange={(e) => setNewSkillName(e.target.value)}
                  placeholder="e.g. Kotlin, Apache Kafka, GraphQL"
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Skill Category
                </label>
                <select
                  value={newSkillCategory}
                  onChange={(e) => setNewSkillCategory(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                >
                  <option value="PROGRAMMING_LANGUAGE">Programming Language</option>
                  <option value="FRAMEWORK">Framework / Library</option>
                  <option value="DATABASE">Database</option>
                  <option value="CLOUD">Cloud &amp; DevOps</option>
                  <option value="TOOL">Tool / Utility</option>
                  <option value="METHODOLOGY">Methodology</option>
                  <option value="OTHER">Other Technical Skill</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                  Aliases &amp; Synonyms (comma separated)
                </label>
                <input
                  type="text"
                  value={newSkillAliases}
                  onChange={(e) => setNewSkillAliases(e.target.value)}
                  placeholder="e.g. kt, kafka, gql"
                  className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs"
                />
                <span className="text-[10px] text-slate-400 mt-1 block">
                  When any of these aliases appear in resumes or job specs, the parser maps them to the canonical name.
                </span>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold disabled:opacity-50"
                >
                  {submitting ? 'Adding...' : 'Add to Taxonomy'}
                </button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </DashboardLayout>
  );
};

export default SkillDictionaryPage;
