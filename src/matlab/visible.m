function b = visible(P1, P2, lines)

N = size(lines, 2);

for i = 1:N
    [~, t1, t2] = line_line_intersect([P1; P2], lines(:, i));
    
    if isnan(t1) % parallel
        continue;
    elseif min(t1, t2) > 0 && max(t1, t2) < 1
        b = false;
        return;
    end
end

b = true;
        