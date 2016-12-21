def transform(old):
    return collect(format(extract(old)))


def format(pair_list):
    return [pair.get_pair_with_lowercase_word() for pair in pair_list]


def extract(old):
    return [sublist for (score, word_list) in old.items() for sublist in split_into_pairs(score, word_list)]


def collect(flattened):
    return dict([([pair.get_word(), pair.get_score()]) for pair in flattened ])


def split_into_pairs(score, word_list):
    return [Pair(score, word) for word in word_list]


class Pair:
    def __init__(self, score, word):
        self.score = score
        self.word = word

    def get_word(self):
        return self.word

    def get_score(self):
        return self.score

    def get_pair_with_lowercase_word(self):
        return Pair(self.score, self.word.lower())
