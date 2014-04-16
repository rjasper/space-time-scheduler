function b = visible(P1, P2, l)

n = size(l, 2);

t1 = NaN(1, n);
t2 = NaN(1, n);

parfor i = 1:n
    [~, t1_i, t2_i] = line_line_intersect([P1; P2], l(:, i));
    
    t1(i) = t1_i;
    t2(i) = t2_i;
end

b = ~any( min(t1, t2) > 0 & max(t1, t2) < 1 );