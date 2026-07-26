import React from 'react';

export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = [];
  const maxVisible = 5;
  let start = Math.max(0, page - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages, start + maxVisible);
  if (end - start < maxVisible) start = Math.max(0, end - maxVisible);

  for (let i = start; i < end; i++) pages.push(i);

  return (
    <div className="pagination">
      <button disabled={page === 0} onClick={() => onPageChange(0)}>&laquo;</button>
      <button disabled={page === 0} onClick={() => onPageChange(page - 1)}>&lsaquo;</button>
      {start > 0 && <span className="page-info">...</span>}
      {pages.map(p => (
        <button key={p} className={p === page ? 'active' : ''} onClick={() => onPageChange(p)}>
          {p + 1}
        </button>
      ))}
      {end < totalPages && <span className="page-info">...</span>}
      <button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>&rsaquo;</button>
      <button disabled={page >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)}>&raquo;</button>
    </div>
  );
}
