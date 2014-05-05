function draw_polygon(P, varargin)

was_hold = ishold;

if iscell(P)
    cellfun(@helper, P);
else
    helper(P);
end

if ~was_hold
    hold off;
end

    function helper(P)
        if isempty(P)
            return;
        end

        X = P(1, :);
        Y = P(2, :);

        fill(X, Y, varargin{:});
        hold on;
    end
end