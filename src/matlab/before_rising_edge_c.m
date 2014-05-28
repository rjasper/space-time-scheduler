function idx = before_rising_edge_c(b, cur)

n_b = length(b);

% left rotation by cur-1
b_ = circshift(b, [0, -(cur-1)]);

% find the element before the next true element after the current one
idx_ = find(~b_ & b_([2:end 1]), 1, 'first');

idx = idxmod(idx_ + cur-1, n_b);

end