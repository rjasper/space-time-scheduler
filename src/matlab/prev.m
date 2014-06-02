function idx = prev(b, cur, varargin)

iscircular = ismember('circular', varargin);

n_b = length(b);

if iscircular
    b_ = circshift(b, [0 -(cur-1)]);
else
    b_ = b(1:cur-1);
end

idx_ = find(b_, 1, 'last');

if isempty(idx_)
    idx = zeros(1, 0);
elseif iscircular
    idx = idxmod(idx_ + cur-1, n_b);
else
    idx = idx_;
end

end