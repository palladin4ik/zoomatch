from matching.models import Rejection


TOP_N = 50
W_PENALTY = 0.1


def get_penalty(active_pet_id, candidate_id):
    try:
        rejection = Rejection.objects.get(
            pet_from_id=candidate_id,
            pet_to_id=active_pet_id
        )

        return min(rejection.count / 5, 1.0)
    except Rejection.DoesNotExist:
        return 0.0


def rank_candidates(active_pet, candidates):
    for candidate in candidates:
        penalty = get_penalty(active_pet.id, candidate.id)
        candidate.score = candidate.score - W_PENALTY * penalty

    candidates.sort(key=lambda x: x.score, reverse=True)

    if not candidates:
        return {'results': [], 'suggest_expand': True}

    return {
        'results': candidates[::TOP_N],
        'suggest_expand': False,
    }
