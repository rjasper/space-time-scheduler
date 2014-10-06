function [merged, idx] = unroll(cells)

merged = [cells{:}];
idx = cellfun( ...
    @(c, i) repmat(i, 1, size(c, 2)), ...
    cells, ...
    num2cell( 1:length(cells) ), ...
    'UniformOutput', false);
idx = [idx{:}];

% if iscell(cells)
%     idx = num2cell([idx{:}]);
% else
%     idx = idx{:};
% end

% mat = [id; cells];

end