function mat = unroll(cells)

id = cellfun( ...
    @(c, i) repmat(i, 1, size(c, 2)), ...
    cells, ...
    num2cell( 1:length(cells) ), ...
    'UniformOutput', false);

cells = [cells{:}];

if iscell(cells)
    id = num2cell([id{:}]);
else
    id = id{:};
end

mat = [id; cells];

end