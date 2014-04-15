function path = pred2path(pred, fid)

N = length(pred);
path = NaN(1, N);

cur = fid;
i = 1;
while cur ~= 0
    path(i) = cur;
    cur = pred(cur);
    i = i + 1;
end

path = fliplr( path(1:i-1) );