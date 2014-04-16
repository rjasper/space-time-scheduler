function e = get_edges(P)

if iscell(P)
    e = cellfun(@helper, P, 'UniformOutput', false);
else
    e = helper(P);
end

function e = helper(P)

L1 = P;
L2 = circshift(P, [0 -1]);

e = [L1; L2];