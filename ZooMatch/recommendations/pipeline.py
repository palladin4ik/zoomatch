from .phases.generation import generate_candidates
from .phases.expansion import expand_candidates
from .phases.filtering import apply_soft_filters
from .phases.scoring import score_candidates
from .phases.ranking import rank_candidates


def run_recommendation_pipeline(active_pet, params):
    radius_km = params.get('radius_km')

    c0 = generate_candidates(active_pet, radius_km)

    if not c0:
        return {'results': [], 'suggest_expand': True}

    c1 = expand_candidates(active_pet, c0)

    c1_filtered = apply_soft_filters(c1, params)

    if not c1_filtered:
        return {'results': [], 'suggest_expand': True}

    scored = score_candidates(active_pet, c1_filtered)

    result = rank_candidates(active_pet, scored)

    return result
