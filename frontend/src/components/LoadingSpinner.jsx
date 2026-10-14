import React from 'react';
import { Loader2 } from 'lucide-react';

const LoadingSpinner = ({ text = 'Loading...', size = 'md' }) => {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center p-8 text-center">
      <Loader2 className={`animate-spin text-blue-600 dark:text-blue-400 ${sizeMap[size] || sizeMap.md}`} />
      {text && <p className="mt-3 text-sm text-slate-500 dark:text-slate-400 font-medium">{text}</p>}
    </div>
  );
};

export default LoadingSpinner;
