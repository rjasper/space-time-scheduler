function inverse = inverse_order(order, m)
    
n = size(order, 2);

if ~exist('m', 'var')
    m = max(order);
end

inverse = zeros(1, m+1);

zero_filt = order == 0;
order(zero_filt) = m+1;
inverse(order) = 1:n;

inverse = inverse(1:m);