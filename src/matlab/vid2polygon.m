function P = vid2polygon(V, vid)

P = cellfun(@(v) V(:, v), vid, 'UniformOutput', false);

end