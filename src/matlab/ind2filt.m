function filt = ind2filt(ind, n)

if ~exist('n', 'var')
    n = max(ind);
end

filt = false(1, n);
filt(ind) = true;

end