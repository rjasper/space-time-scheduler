function draw_polygon(P)

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

X = P(1, :);
Y = P(2, :);

fill(X, Y, 'red');
hold on;