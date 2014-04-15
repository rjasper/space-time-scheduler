function draw_obstacle(Om)

was_hold = ishold;

for i = 1:length(Om)
    X = Om{i}(1, :);
    Y = Om{i}(2, :);
    
    fill(X, Y, 'red');
    hold on;
end

if ~was_hold
    hold off;
end