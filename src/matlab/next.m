function idx = next(b, cur, varargin)

iscircular = ismember('circular', varargin);

n_b = length(b);

if iscircular
    b_ = circshift(b, [0 -cur]);
else
    b_ = b(cur+1:end);
end

idx_ = find(b_, 1, 'first');

if isempty(idx_)
    idx = zeros(1, 0);
elseif iscircular
    idx = idxmod(idx_ + cur, n_b);
else
    idx = idx_ + cur;
end

end